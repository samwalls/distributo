package uk.ac.st_andrews.distributo.lib.receiver;

import uk.ac.st_andrews.distributo.lib.protocol.*;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class Receiver implements Runnable {

    private MulticastSocket dataSocket;

    private InetSocketAddress controlAddr;
    private InetAddress groupAddr;
    private int groupPort, controlPort;

    private File shareRoot;

    private volatile boolean startedListening, acceptingData;

    public Receiver(String shareRoot, String groupHost, int groupPort, String controlHost, int controlPort) throws IOException {
        this.shareRoot = new File(shareRoot);
        if (!this.shareRoot.isDirectory())
            throw new IOException(shareRoot + " is not a directory");
        this.groupPort = groupPort;
        this.controlPort = controlPort;
        controlAddr = new InetSocketAddress(controlHost, controlPort);
        groupAddr = InetAddress.getByName(groupHost);
        dataSocket = new MulticastSocket(groupPort);
        //create a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("received shutdown signal");
                deregisterWithControl();
                stopAcceptingData();
            } catch (IOException e) {
                System.err.println("error when shutting down");
                e.printStackTrace();
            }
        }));
    }

    @Override
    public void run() {
        try {
            FileInfo info = registerWithControl();
            System.out.println("waiting for data transmission to finish");
            acceptData(info);
            //deregistration is handled in the shutdown hook
        } catch (SocketException e) {
            //this is actually OK, but might want to report this anyway
            System.out.println("multicast socket was closed while waiting");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a registration request to the control server.
     * @return the file info sent back from the control server
     * @throws IOException if there was a problem talking to the server, or the received packet type was unexpected
     */
    private synchronized FileInfo registerWithControl() throws IOException {
        try (Socket controlSocket = new Socket()) {
            System.out.println("connecting to control: " + controlAddr.toString());
            //ten second timeout
            controlSocket.setSoTimeout(10000);
            controlSocket.connect(controlAddr);
            //register this receiver
            System.out.println("requesting registration from control");
            new Packet(PacketType.RECEIVER_REGISTER).writeToStream(controlSocket.getOutputStream());
            //read the packet from the connection
            System.out.println("awaiting control responses...");
            Packet p = Packet.fromStream(controlSocket.getInputStream());
            System.out.printf("[%s]: <%s>\n", controlSocket.getInetAddress().toString(), p.toString());
            switch (p.type()) {
                case RECEIVER_REGISTER_ACK:
                    FileInfo sentInfo = new FileInfo();
                    sentInfo.unmarshal(p.data());
                    System.out.printf("RECEIVER_REGISTER_ACK included information:\n- %s\n- %s\n", sentInfo.fileLength(), sentInfo.relativePath());
                    return sentInfo;
                case ERROR:
                    //die
                    //todo
                    throw new IOException(new String(p.data()));
            }
            throw new IOException("unexpected packet type: " + p.type().name());
        } catch (IOException e) {
            //let resources automatically close, rethrow the exception to the caller
            throw e;
        }
    }

    /**
     * Send a deregistration request to the control server, no need to listen for responses.
     */
    private synchronized void deregisterWithControl() throws IOException {
        try (Socket controlSocket = new Socket()) {
            System.out.println("connecting to control: " + controlAddr.toString());
            controlSocket.connect(controlAddr);
            //register this receiver
            System.out.println("requesting deregistration from control");
            new Packet(PacketType.RECEIVER_DEREGISTER).writeToStream(controlSocket.getOutputStream());
        } catch (IOException e) {
            //let resources automatically close, rethrow the exception to the caller
            throw e;
        }
    }

    private synchronized void postDataNackAsync(List<PacketRange> missing) {
        new Thread(() -> {
            try (Socket controlSocket = new Socket()) {
                //System.out.println("sending DATA_NACK to control server");
                //timeout of 10 seconds, the sender must have died if this is the case
                controlSocket.setSoTimeout(10000);
                controlSocket.connect(controlAddr);
                Packet.makeDataNackPacket(missing).writeToStream(controlSocket.getOutputStream());
                //System.out.println("awaiting control server response");
                Packet p = Packet.fromStream(controlSocket.getInputStream());
                //System.out.printf("[%s]: <%s>\n", controlSocket.getInetAddress().toString(), p.toString());
                if (p.type() != PacketType.DATA_NACK_ACK)
                    throw new IOException("unexpected packet type: " + p.type().name());
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    stopAcceptingData();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Start listening for file data, based upon the passed {@link FileInfo}.
     * @param fileInfo information about the file to read (size, relative path etc.)
     * @throws IOException if there was a problem talking to the data server
     */
    private void acceptData(FileInfo fileInfo) throws IOException {
        acceptingData = true;
        System.out.printf("joining group: %s on %s\n", groupAddr.toString(), groupPort);
        dataSocket.joinGroup(groupAddr);
        byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
        //System.out.println("max packet size " + Packet.MAX_PACKET_SIZE);
        FileMerger merger = new FileMerger(shareRoot, fileInfo);
        long packetnoDelta = 0;
        while (merger.anyMissing()) {
            //System.out.println("missing packets :\n" + merger.toString());
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            dataSocket.receive(datagramPacket);
            Packet p = new Packet();
            p.unmarshal(buffer);
            packetnoDelta = p.packetno() - packetnoDelta;
            if (merger.isMissing(p.packetno()))
                merger.writePacket(p);
            //reset buffer
            buffer = new byte[Packet.MAX_PACKET_SIZE];
            //if (packetnoDelta++ % 20 == 0)
            //postDataNackAsync(merger.getMissing());
        }
    }

    private void stopAcceptingData() throws IOException {
        acceptingData = false;
        if (!dataSocket.isClosed())
            dataSocket.close();
    }
}
