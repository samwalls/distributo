package uk.ac.st_andrews.distributo.lib.sender;

import uk.ac.st_andrews.distributo.lib.protocol.Packet;
import uk.ac.st_andrews.distributo.lib.protocol.PacketRange;
import uk.ac.st_andrews.distributo.lib.protocol.PacketType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.List;

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
            //System.out.println("client handler started for " + client.getInetAddress().toString() + " on " + client.getLocalPort());
            Packet p = Packet.fromStream(client.getInputStream());
            switch (p.type()) {
                case RECEIVER_REGISTER:
                    System.out.printf("[%s]: <%s>\n", client.getInetAddress().getHostName(), p.toString());
                    //respond with RECEIVER_REGISTER_ACK, along with file information
                    System.out.println("registering client " + client.getInetAddress().toString() + " to receive");
                    sender.registerClient(client);
                    Packet response = Packet.makeRegisterAckPacket(sender.file().length(), sender.file().getName());
                    response.writeToStream(client.getOutputStream());
                    break;
                case RECEIVER_DEREGISTER:
                    System.out.printf("[%s]: <%s>\n", client.getInetAddress().getHostName(), p.toString());
                    System.out.println("deregistering client " + client.getInetAddress().toString());
                    sender.deregisterClient(client.getInetAddress());
                    break;
                case DATA_NACK:
                    //Currently don't need to do anything except send back an acknowledgement, as we don't use the sent
                    //information.
                    /*
                    byte[] data = p.data();
                    List<PacketRange> missing;
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
                        try (ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
                            missing = (List<PacketRange>)ois.readObject();
                        }
                    }
                    */
                    //write acknowledgement response
                    new Packet(PacketType.DATA_NACK_ACK).writeToStream(client.getOutputStream());
            }
        } catch (IOException e) {
            //todo notify the parent sender object that this packet was borked
            e.printStackTrace();
        }
    }
}
