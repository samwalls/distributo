package uk.ac.st_andrews.distributo.lib;

public interface Marshallable {

    /**
     * Marshal the object into binary data.
     * @return the relevant binary representation associated with the instance of type T
     * @throws MarshalException if the input instance cannot produce a data representation
     */
    byte[] marshal() throws MarshalException;

    /**
     * Unmarshal binary data into a valid instance of the object.
     * @param data binary data to unmarshal
     * @throws UnmarshalException if the input data cannot produce an instance of type {@link T}
     */
    void unmarshal(byte[] data) throws UnmarshalException;
}
