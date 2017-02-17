package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * An enumeration of all the packet types that can be sent over the distributo protocol.
 */
public enum PacketType {

    /**
     * A packet with no type information.
     */
    NONE,

    /**
     * A receiver calls to join the distributo group.
     */
    RECEIVER_REGISTER,

    /**
     * The sender acknowledges the call to {@link PacketType#RECEIVER_REGISTER}. The data field will contain the size of
     * the file that will be sent (the number of packets can be algorithmically defined from the size).
     */
    RECEIVER_REGISTER_ACK,

    /**
     * A receiver withdraws from the distributo group. This does not need to be outwardly acknowledged by the sender.
     * This is also sent by the receiver when it has all the packets it needs, and drops out.
     */
    RECEIVER_DEREGISTER,

    /**
     * Incoming file data from the sender.
     */
    DATA,

    /**
     * An error occurred; either on the end of the receiver or the sender - the {@link Packet#data()} field
     * consists of a string with the error information.
     */
    ERROR
}
