package uk.ac.st_andrews.distributo.lib.protocol;

import uk.ac.st_andrews.distributo.lib.MarshalException;
import uk.ac.st_andrews.distributo.lib.Marshallable;
import uk.ac.st_andrews.distributo.lib.UnmarshalException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * An encapsulation of the packet format distributo uses. This contains both header information and raw data. This class
 * also implements the {@link Marshallable} interface, performing the necessary bit-twiddling to transmit packets over
 * streams/sockets etc..
 */
public class Packet implements Marshallable, Cloneable {

    /**
     * The length of the checksum field in bytes.
     */
    private static int CHECKSUM_LENGTH = Long.BYTES;

    /**
     * The number of bytes to determine the type of packet.
     */
    private static int TYPE_LENGTH = 1;

    /**
     * The number of bytes to determine the packet number (if applicable to the packet type)
     */
    private static int PACKETNO_LENGTH = Long.BYTES;

    /**
     * The number of bytes to determine the length of data included in the packet.
     */
    private static int DATALEN_LENGTH = Integer.BYTES;

    /**
     * The maximum size of a packet including all header information.
     *
     * I.e. http://stackoverflow.com/questions/1098897/what-is-the-largest-safe-udp-packet-size-on-the-internet
     */
    public static int MAX_PACKET_SIZE = 65507;

    /**
     * A packet must contain this many bytes at a minimum, otherwise the header can only be malformed.
     */
    private static int MIN_PACKET_SIZE = CHECKSUM_LENGTH + TYPE_LENGTH + PACKETNO_LENGTH + DATALEN_LENGTH;

    /**
     * The maximum amount of extra data that can be added to a packet. This is based on the fact that the minimum amount
     * of extra data a packet can contain is 0 (i.e. for acknowledgements).
     */
    public static int MAX_DATA_SIZE = MAX_PACKET_SIZE - MIN_PACKET_SIZE;

    /*-------- MEMBER VARIABLES --------*/

    private PacketType _type;

    private byte[] _data;

    private long _packetno;

    /*-------- STATIC HELPERS --------*/

    /**
     * @param data data to generate a CRC checksum for
     * @return the CRC value, as a byte array, for the passed data
     */
    private static byte[] getCRC(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return getBytes(crc.getValue());
    }

    /**
     * Check the CRC contained in the passed data with the CRC obtained over the rest of the data.
     * @param data full packet data including the CRC, and all header information, data etc.
     * @return true if the contained CRC is the same as the calculated one
     */
    public static boolean checkCRC(byte[] data) {
        long sentCRC = getLong(Arrays.copyOfRange(data, 0, CHECKSUM_LENGTH));
        byte[] dataWithoutCRC = Arrays.copyOfRange(data, CHECKSUM_LENGTH, data.length);
        long calculatedCRC = getLong(getCRC(dataWithoutCRC));
        return sentCRC == calculatedCRC;
    }

    /**
     * Join the two byte arrays.
     * @param a
     * @param b
     * @return a byte array containing all elements of a, combined with all the elements of b
     */
    static byte[] merge(byte[] a, byte[] b) {
        byte[] total = new byte[a.length + b.length];
        System.arraycopy(a, 0, total, 0, a.length);
        System.arraycopy(b, 0, total, a.length, b.length);
        return total;
    }

    /**
     * @param value
     * @return a byte array consisting of the value data
     */
    static byte[] getBytes(long value) {
        return ByteBuffer.allocate(Long.BYTES).putLong(value).array();
    }

    /**
     * @param value
     * @return a byte array consisting of the value data
     */
    static byte[] getBytes(int value) {
        return ByteBuffer.allocate(Integer.BYTES).putInt(value).array();
    }

    /**
     * @param bytes
     * @return the long value represented by the passed bytes
     */
    static long getLong(byte[] bytes) {
        if (bytes == null)
            throw new IllegalArgumentException("cannot get long value from null byte array");
        if (bytes.length == 0)
            throw new IllegalArgumentException("cannot get long value from empty byte array");
        if (bytes.length > Long.BYTES)
            throw new IllegalArgumentException("too many bytes to get long from");
        return ByteBuffer.wrap(bytes).getLong();
    }

    /**
     * @param bytes
     * @return the int value represented by the passed bytes
     */
    static int getInt(byte[] bytes) {
        if (bytes == null)
            throw new IllegalArgumentException("cannot get int value from null byte array");
        if (bytes.length == 0)
            throw new IllegalArgumentException("cannot get int value from empty byte array");
        if (bytes.length > Integer.BYTES)
            throw new IllegalArgumentException("too many bytes to get int from");
        return ByteBuffer.wrap(bytes).getInt();
    }

    private static boolean usesData(PacketType type) {
        return type == PacketType.DATA || type == PacketType.ERROR || type == PacketType.RECEIVER_REGISTER_ACK;
    }

    /*-------- CONSTRUCTOR --------*/

    /**
     * Constructor for packet type, packets can also be created via the static factory methods
     * {@link Packet#makeDataPacket(long, byte[])} etc..<br>
     * <br>
     * An example illustration of the data a packet contains:<br>
     * <pre>
     * ------------------------
     * |0-7|8|9-17|18-22|23...|
     * ------------------------
     * |CCC|T|NNNN|LLLLL|DD...|
     * ------------------------
     * </pre>
     *
     * Where:
     * <ul>
     *     <li>C is checksum data</li>
     *     <li>T is packet type data</li>
     *     <li>N is packet number data</li>
     *     <li>L is data length data</li>
     *     <li>D is data included with the packet</li>
     * </ul>
     *
     * @param type the type identifier of the constructed packet
     */
    public Packet(PacketType type) {
        _type = type;
    }

    /**
     * Constructor for a packet with no type information.
     */
    public Packet() {
        this(PacketType.NONE);
    }

    /*-------- FACTORY METHODS --------*/

    /**
     * @param packetno the ordinal number associated with this packet
     * @param data the data to include in the packet
     * @return a fully formed {@link Packet} object
     */
    public static Packet makeDataPacket(long packetno, byte[] data) {
        Packet p = new Packet(PacketType.DATA);
        p._packetno = packetno;
        p._data = data;
        return p;
    }

    /**
     * @param error the error message to send
     * @return a fully formed packet object
     */
    public static Packet makeErrorPacket(String error) {
        Packet p = new Packet(PacketType.ERROR);
        p._data = error.getBytes();
        return p;
    }

    /**
     * @param fileSize the length of the file that the receivers should get in bytes
     * @param path the relative path from the receiver's sharing root of the received file-to-be.
     * @return a fully formed packet object
     */
    public static Packet makeRegisterAckPacket(long fileSize, String path) throws MarshalException {
        Packet p = new Packet(PacketType.RECEIVER_REGISTER_ACK);
        p._data = (new FileInfo(fileSize, path)).marshal();
        return p;
    }

    /**
     * Create a packet from an input stream. The input format of the packet is analogous to that which is created by
     * {@link Packet#writeToStream(OutputStream)}.
     * @param input the input stream to read from
     * @return a fully formed packet object
     * @throws IOException
     * @throws UnmarshalException
     */
    public static Packet fromStream(InputStream input) throws IOException, UnmarshalException {
        DataInputStream dis = new DataInputStream(input);
        byte[] data = new byte[MAX_PACKET_SIZE];
        dis.readFully(data);
        Packet p = new Packet();
        p.unmarshal(data);
        return p;
    }

    /*-------- MEMBER METHODS --------*/

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        else if (!(other instanceof Packet))
            return false;
        Packet p = (Packet)other;
        boolean typeEqual = p._type == _type;
        boolean hasData = _type == PacketType.DATA || _type == PacketType.ERROR;
        boolean hasPacketNo = _type == PacketType.DATA;
        boolean dataEqual = Arrays.equals(p._data, _data);
        return typeEqual && (!hasData || dataEqual) && (!hasPacketNo || _packetno == p._packetno);
    }

    @Override
    public Packet clone() {
        Packet p = new Packet(type());
        p._data = _data;
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
    public byte[] marshal() throws MarshalException {
        if (_data != null && _data.length > MAX_DATA_SIZE)
            throw new MarshalException("packet data is too large to marshal; maximum is " + MAX_DATA_SIZE + "B - got " + _data.length + "B");
        byte[] bytes = new byte[0];
        //add the type byte - this will fail if PacketType has more than 2^8 - 1 ordinals
        bytes = merge(bytes, new byte[] {(byte)type().ordinal()});
        //write the packet number, we only need it however if we're sending data
        bytes = merge(bytes, getBytes(_packetno));
        //add bytes for the data length
        int datalen = _data == null ? 0 : _data.length;
        bytes = merge(bytes, getBytes(datalen));
        //add the data's bytes itself
        if (_data != null)
            bytes = merge(bytes, _data);
        //pad with 0s to the full length if necessary
        int delta = _data == null ? MAX_DATA_SIZE : MAX_DATA_SIZE - _data.length;
        if (delta >= 0) {
            byte[] pad = new byte[delta];
            bytes = merge(bytes, pad);
        }
        //prepend a checksum
        bytes = merge(getCRC(bytes), bytes);
        //assertion about the maximum size
        if (bytes.length > MAX_PACKET_SIZE)
            throw new MarshalException("marshalled packet exceeds maximum size: MAX_PACKET_SIZE = " + MAX_PACKET_SIZE + "; packet size = " + bytes.length);
        if (bytes.length < MAX_PACKET_SIZE)
            throw new MarshalException("marshalled packet was not padded out of maximum size: MAX_PACKET_SIZE = " + MAX_PACKET_SIZE + "; packet size = " + bytes.length);
        return bytes;
    }

    /**
     * Unmarshal binary data into this packet instance.
     * @param data binary data to unmarshal
     * @throws UnmarshalException if the binary data is borked: does not represent a valid {@link Packet} instance
     */
    @Override
    public void unmarshal(byte[] data) throws UnmarshalException {
        if (data == null)
            throw new UnmarshalException("packet is malformed: packet is null");
        else if (data.length < MIN_PACKET_SIZE)
            throw new UnmarshalException("packet is malformed: doesn't meet the minimum packet size");
        else if (data.length > MAX_PACKET_SIZE)
            throw new UnmarshalException("packet is malformed: exceeds maximum allowed size");
        else if (data.length < MAX_PACKET_SIZE)
            throw new UnmarshalException("packet is malformed: isn't padded out to the maximum packet size");
        else if (!checkCRC(data))
            throw new InvalidCRCException("CRC checksums differ");
        int pos = CHECKSUM_LENGTH;
        //get a type byte
        byte typeByte = data[pos];
        PacketType sentType = PacketType.values()[typeByte];
        pos += TYPE_LENGTH;
        //get packet number data
        long packetno = getLong(Arrays.copyOfRange(data, pos, pos + PACKETNO_LENGTH));
        pos += PACKETNO_LENGTH;
        //get the data length
        int datalen = getInt(Arrays.copyOfRange(data, pos, pos + DATALEN_LENGTH));
        pos += DATALEN_LENGTH;
        //now we have the data - modify this object with it
        _type = sentType;
        switch (_type) {
            case DATA:
                _packetno = packetno;
            case RECEIVER_REGISTER_ACK:
            case ERROR:
                _data = Arrays.copyOfRange(data, pos, pos + datalen);
                break;
        }
    }

    /**
     * Write this packet to the passed output stream. The packet can be reconstructed using
     * {@link Packet#fromStream(InputStream)}.
     * @param output the output stream to write to
     * @throws IOException if the output stream is null, or for any other reason an output stream might throw an error
     * @throws MarshalException if marshalling fails
     */
    public void writeToStream(OutputStream output) throws IOException, MarshalException {
        if (output == null)
            throw new IOException("cannot write packet to null socket");
        byte[] data = marshal();
        DataOutputStream dos = new DataOutputStream(output);
        dos.write(data);
    }

    /**
     * @return the type of this packet
     */
    public PacketType type() {
        return this._type;
    }

    /**
     * @return the data contents of this packet
     */
    public byte[] data() {
        return this._data;
    }

    /**
     * @return the packet number of this packet
     */
    public long packetno() {
        return _packetno;
    }
}
