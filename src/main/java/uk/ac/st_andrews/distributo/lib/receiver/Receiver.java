package uk.ac.st_andrews.distributo.lib.receiver;

import uk.ac.st_andrews.distributo.lib.protocol.FileInfo;
import uk.ac.st_andrews.distributo.lib.protocol.FileMerger;
import uk.ac.st_andrews.distributo.lib.protocol.Packet;
import uk.ac.st_andrews.distributo.lib.protocol.PacketType;

import java.io.File;
import java.io.IOException;
import java.net.*;

public class Receiver implements Runnable {

    private MulticastSocket dataSocket;
    private Socket controlSocket;

    private InetSocketAddress controlAddr;
    private InetAddress groupAddr;
    private int groupPort, controlPort;

    private FileInfo sentInfo;
    private File shareRoot;
    private FileMerger merger;

    private volatile boolean acceptingControl, acceptingData;

    private Thread dataThread;

    public Receiver(String shareRoot, String groupHost, int groupPort, String controlHost, int controlPort) throws IOException {
        this.shareRoot = new File(shareRoot);
        if (!this.shareRoot.isDirectory())
            throw new IOException(shareRoot + " is not a directory");
        this.groupPort = groupPort;
        this.controlPort = controlPort;
        controlAddr = new InetSocketAddress(controlHost, controlPort);
        groupAddr = InetAddress.getByName(groupHost);
        dataSocket = new MulticastSocket(groupPort);
        controlSocket = new Socket();
        dataThread = new Thread(() -> {
            try {
                acceptData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //create a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("received shutdown signal");
                deregister();
                stopAcceptingControl();
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
            acceptControl();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAcceptingControl() throws IOException {
        acceptingControl = false;
        if (!controlSocket.isClosed()) {
            deregister();
            controlSocket.close();
        }
    }

    private void acceptControl() throws IOException {
        acceptingControl = true;
        System.out.println("connecting to control: " + controlAddr.toString());
        controlSocket.connect(controlAddr);
        //register this receiver
        System.out.println("requesting registration from control");
        new Packet(PacketType.RECEIVER_REGISTER).writeToStream(controlSocket.getOutputStream());
        while (acceptingControl) {
            //read the packet from the connection
            System.out.println("awaiting control responses...");
            Packet p = Packet.fromStream(controlSocket.getInputStream());
            System.out.printf("[%s]: <%s>\n", controlSocket.getInetAddress().toString(), p.toString());
            switch (p.type()) {
                case RECEIVER_REGISTER_ACK:
                    sentInfo = new FileInfo();
                    sentInfo.unmarshal(p.data());
                    System.out.printf("RECEIVER_REGISTER_ACK included information:\n- %s\n- %s\n", sentInfo.fileLength(), sentInfo.relativePath());
                    //start accepting data
                    dataThread.start();
                    break;
                case ERROR:
                    //die
                    stopAcceptingData();
                    stopAcceptingControl();
                    throw new IOException(new String(p.data()));
            }
        }
    }

    private void stopAcceptingData() throws IOException {
        boolean acceptingBefore = acceptingData;
        acceptingData = false;
        if (acceptingBefore || !dataSocket.isClosed())
            dataSocket.leaveGroup(groupAddr);
        if (!dataSocket.isClosed())
            dataSocket.close();
    }

    private void acceptData() throws IOException {
        acceptingData = true;
        System.out.printf("joining group: %s on %s\n", groupAddr.toString(), groupPort);
        dataSocket.joinGroup(groupAddr);
        byte[] buffer = new byte[Packet.MAX_PACKET_SIZE];
        System.out.println("max packet size " + Packet.MAX_PACKET_SIZE);
        merger = new FileMerger(shareRoot, sentInfo);
        while (acceptingData) try {
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            dataSocket.receive(datagramPacket);
            Packet p = new Packet();
            p.unmarshal(buffer);
            merger.writePacket(p);
            //reset buffer
            buffer = new byte[Packet.MAX_PACKET_SIZE];
        } catch(IOException e) {
            System.err.println("error when accepting data");
            e.printStackTrace();
            //stopAcceptingData();
            //throw e;
        }
    }

    private synchronized void deregister() throws IOException {
        System.out.println("deregistering with control server");
        Packet p = new Packet(PacketType.RECEIVER_DEREGISTER);
        p.writeToStream(controlSocket.getOutputStream());
    }
}
