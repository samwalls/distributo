package uk.ac.st_andrews.distributo.lib;

import uk.ac.st_andrews.distributo.lib.protocol.Packet;
import uk.ac.st_andrews.distributo.lib.protocol.PacketType;
import uk.ac.st_andrews.distributo.lib.protocol.UnmarshallException;

import java.util.Arrays;
import java.util.List;

public class TestPacket extends TestMarshallable<Packet> {

    @Override
    public List<Packet> makeNormalInput() {
        return Arrays.asList(
                new Packet(PacketType.RECEIVER_REGISTER),
                new Packet(PacketType.RECEIVER_REGISTER_ACK),
                new Packet(PacketType.RECEIVER_DEREGISTER),
                new Packet(PacketType.RECEIVER_FINISH),
                Packet.makeDataPacket("/blogs/joesblog1.html", 0, new byte[] {1, 2, 3, 4, 5}),
                Packet.makeErrorPacket("\"/blogs/joesblog1.html\" does not exist")
        );
    }

    @Override
    public List<Packet> makeExtremeInput() {
        return Arrays.asList(new Packet[] {

        });
    }

    @Override
    public List<Packet> makeMarshallExceptionalInput() {
        return null;
    }

    @Override
    public List<byte[]> makeUnmarshallExceptionalInput() {
        return null;
    }

    @Override
    public Packet makeFromUnmarshall(byte[] data) throws UnmarshallException {
        Packet p = new Packet(PacketType.DATA);
        p.unmarshall(data);
        return p;
    }

    @Override
    public Packet copy(Packet instance) {
        return instance.clone();
    }
}
