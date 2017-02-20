package uk.ac.st_andrews.distributo.lib.sender;

import uk.ac.st_andrews.distributo.lib.protocol.Packet;

import java.io.IOException;
import java.net.Socket;

public class ReceiverControlHandler implements Runnable {

    private Socket client;
    private Sender sender;

    /**
     * @param client the client socket to deal with
     * @param sender the sender that generated this handler
     */
    public ReceiverControlHandler(Socket client, Sender sender) {
        this.client = client;
        this.sender = sender;
    }

    @Override
    public void run() {
        try {
            System.out.println("client handler started for " + client.getInetAddress().toString() + " on " + client.getLocalPort());
            Packet p = Packet.fromStream(client.getInputStream());
            System.out.printf("[%s]: received packet <%s>\n", client.getInetAddress().toString(), p.toString());
            switch (p.type()) {
                case RECEIVER_REGISTER:
                    //respond with RECEIVER_REGISTER_ACK, along with file information
                    System.out.println("registering client " + client.getInetAddress().toString() + " to receive");
                    sender.registerClient(client);
                    Packet response = Packet.makeRegisterAckPacket(sender.file().length(), "test.txt");
                    response.writeToStream(client.getOutputStream());
                    break;
                case RECEIVER_DEREGISTER:
                    System.out.println("deregistering client " + client.getInetAddress().toString());
                    sender.deregisterClient(client.getInetAddress());
                    break;
            }
        } catch (IOException e) {
            //todo notify the parent sender object that this packet was borked
            e.printStackTrace();
        }
    }
}
