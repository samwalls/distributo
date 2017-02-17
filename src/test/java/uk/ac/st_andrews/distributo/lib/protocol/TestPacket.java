package uk.ac.st_andrews.distributo.lib.protocol;

import uk.ac.st_andrews.distributo.lib.TestMarshallable;
import uk.ac.st_andrews.distributo.lib.UnmarshalException;

import java.util.Arrays;
import java.util.List;

public class TestPacket extends TestMarshallable<Packet> {

    @Override
    public List<Packet> makeNormalInput() {
        return Arrays.asList(
                new Packet(PacketType.RECEIVER_REGISTER),
                new Packet(PacketType.RECEIVER_REGISTER_ACK),
                new Packet(PacketType.RECEIVER_DEREGISTER),
                Packet.makeDataPacket(0, new byte[] {1, 2, 3, 4, 5}),
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
        return Arrays.asList(
                //too big
                Packet.makeDataPacket(0, new byte[67000])
        );
    }

    @Override
    public List<byte[]> makeUnmarshallExceptionalInput() {
        String path = "/example";
        byte[] header = new byte[] {0, (byte)path.length()};
        byte[] packet = new byte[Packet.MAX_PACKET_SIZE];
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(path.getBytes(), 0, packet, header.length, path.length());
        return Arrays.asList(
                null,
                new byte[0],
                new byte[] {0},
                new byte[] {0, 1},
                packet
        );
    }

    @Override
    public Packet makeFromUnmarshall(byte[] data) throws UnmarshalException {
        Packet p = new Packet(PacketType.DATA);
        p.unmarshal(data);
        return p;
    }

    @Override
    public Packet copy(Packet instance) {
        return instance.clone();
    }
}
