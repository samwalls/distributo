package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * An enumeration of all the packet types that can be sent over the distributo protocol.
 */
public enum PacketType {

    /**
     * A receiver calls to join the distributo group.
     */
    RECEIVER_REGISTER,

    /**
     * The sender acknowledges the call to {@link PacketType#RECEIVER_REGISTER}.
     */
    RECEIVER_REGISTER_ACK,

    /**
     * A receiver withdraws from the distributo group. This does not need to be outwardly acknowledged by the sender.
     */
    RECEIVER_DEREGISTER,

    /**
     * A receiver now has all the data it needs. The sender needs to know this.
     */
    RECEIVER_FINISH,

    /**
     * Incoming file data from the sender.
     */
    DATA,

    /**
     * An error occurred; either on the end of the receiver or the sender - the {@link Packet#data()} field
     * consists of a string with the error information, and when unmarshalled can be safely accessed with
     * {link Packet#error()}.
     */
    ERROR
}
