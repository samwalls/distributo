package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * An encapsulation of the packet format distributo uses. This contains both header information and raw data.
 */
public class Packet implements Marshallable, Cloneable {

    private PacketType _type;

    private byte[] _data;

    private String _error;

    private FileDescriptor _fileDescriptor;

    /**
     * Constructor for packet type, packets can also be created via the static factory methods
     * {@link Packet#makeDataPacket(String, long, byte[])} etc..
     * @param type the type identifier of the constructed packet
     */
    public Packet(PacketType type) {
        _type = type;
        _data = new byte[0];
    }

    /*-------- FACTORY METHODS --------*/

    /**
     *
     * @param relPath the path of the file relative to the receiver's share root
     * @param chunk the chunk of the file being sent
     * @param data
     * @return
     */
    public static Packet makeDataPacket(String relPath, long chunk, byte[] data) {
        Packet p = new Packet(PacketType.DATA);
        p._data = data;
        return p;
    }

    /**
     *
     * @param error
     * @return
     */
    public static Packet makeErrorPacket(String error) {
        Packet p = new Packet(PacketType.ERROR);
        p._data = error.getBytes();
        return p;
    }

    @Override
    public Packet clone() {
        Packet p = new Packet(type());
        p._data = _data;
        p._error = _error;
        p._fileDescriptor = _fileDescriptor;
        return p;
    }

    @Override
    public String toString() {
        return type().name() + ": " + this.getClass().getSimpleName() + "@" + this.hashCode();
    }

    /**
     * Marshall this packet instances into a binary representation.
     * @return the binary representation - ready for transmission - of a packet
     */
    @Override
    public byte[] marshall() {
        //this will not work if PacketType has more than 2^8 - 1 enum members
        byte typeByte = (byte)type().ordinal();
        byte[] bytes = new byte[data().length + 1];
        bytes[0] = typeByte;
        for (int i = 0; i < data().length; i++)
            bytes[i + 1] = data()[i];
        return bytes;
    }

    /**
     * Unmarshall binary data into this packet instance.
     * @param data binary data to unmarshall
     * @throws UnmarshallException if the binary data is borked: does not represent a valid {@link Packet} instance
     */
    @Override
    public void unmarshall(byte[] data) throws UnmarshallException {
        if (data == null || data.length <= 0)
            throw new UnmarshallException("no data to unmarshall");
        byte typeByte = data[0];
        PacketType[] types = PacketType.values();
        if (typeByte >= types.length)
            throw new UnmarshallException("type byte specifies unknown packet type");
        PacketType type = types[typeByte];
        switch (type) {
            case DATA:
            case ERROR:
        }
        //we have the data, error-free, set up this object with it
        _type = type;
        _data = data;
    }

    public PacketType type() {
        return this._type;
    }

    public byte[] data() {
        return this._data;
    }

    public String error() {
        return _error;
    }
}
