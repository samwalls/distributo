package uk.ac.st_andrews.distributo.lib.node;

import uk.ac.st_andrews.distributo.lib.UnmarshalException;
import uk.ac.st_andrews.distributo.lib.protocol.Packet;

import java.io.IOException;
import java.net.*;

public class ReceiverNode implements Runnable {

    private MulticastSocket dataSocket;
    private Socket controlSocket;

    private InetSocketAddress controlAddr;
    private InetAddress groupAddr;
    private int groupPort, controlPort;

    private volatile boolean acceptingControl, acceptingData;

    private Thread dataThread, controlThread;

    public ReceiverNode(String groupHost, int groupPort, String controlHost, int controlPort) throws IOException {
        this.groupPort = groupPort;
        this.controlPort = controlPort;
        controlAddr = new InetSocketAddress(controlHost, controlPort);
        groupAddr = InetAddress.getByName(groupHost);
        dataSocket = new MulticastSocket(groupPort);
        controlSocket = new Socket();
        controlThread = new Thread(() -> {
            try {
                acceptControl();
            } catch(IOException | UnmarshalException e) {
                e.printStackTrace();
            }
        });
        dataThread = new Thread(() -> {
            try {
                acceptData();
            } catch (IOException | UnmarshalException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void run() {
        controlThread.start();
        dataThread.start();
        try {
            controlThread.join();
        } catch (InterruptedException e) {
            acceptingData = false;
            acceptingControl = false;
            e.printStackTrace();
        }
    }

    private void acceptControl() throws IOException, UnmarshalException {
        acceptingControl = true;
        while (acceptingControl) try {
            controlSocket.connect(controlAddr);
            //read the packet from the connection
            Packet p = Packet.fromStream(controlSocket.getInputStream());
            switch (p.type()) {
                case RECEIVER_REGISTER_ACK:
                case ERROR:
                    System.err.println();
            }
        } catch (IOE    )
    }

    private void acceptData() throws IOException, UnmarshalException {
        acceptingData = true;
        System.out.printf("joining group: %s on %s\n", groupAddr.toString(), groupPort);
        dataSocket.joinGroup(groupAddr);
        while (acceptingData) {
            byte[] buffer = new byte[1000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            dataSocket.receive(packet);
            System.out.printf("got \"%s\" from: %s\n", new String(packet.getData()), packet.getAddress().toString());
        }
        dataSocket.leaveGroup(groupAddr);
        dataSocket.close();
    }
}
