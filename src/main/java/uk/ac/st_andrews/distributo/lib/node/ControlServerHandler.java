package uk.ac.st_andrews.distributo.lib.node;

import uk.ac.st_andrews.distributo.lib.MarshalException;
import uk.ac.st_andrews.distributo.lib.UnmarshalException;
import uk.ac.st_andrews.distributo.lib.protocol.Packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ControlServerHandler implements Runnable {

    private Socket client;
    private FileServerNode server;

    /**
     * @param client the client socket to deal with
     * @param server the server that generated this handler
     */
    public ControlServerHandler(Socket client, FileServerNode server) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            Packet p = readPacket();
            switch (p.type()) {
                case RECEIVER_REGISTER:
            }
        } catch (UnmarshalException | IOException e) {
            //todo notify the parent server that this packet was borked
            e.printStackTrace();
        }
    }

    private Packet readPacket() throws UnmarshalException, IOException {
        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int v;
            while ((v = client.getInputStream().read()) != -1)
                baos.write(v);
            data = baos.toByteArray();
            Packet p = new Packet();
            p.unmarshal(data);
            return p;
        } catch (IOException e) {
            throw e;
        }
    }

    private void writePacket(Packet p) throws MarshalException, IOException {
        try {
            client.getOutputStream().write(p.marshal());
        } catch (IOException e) {
            throw e;
        }
    }
}
