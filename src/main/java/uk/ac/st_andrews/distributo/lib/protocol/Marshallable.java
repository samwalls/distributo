package uk.ac.st_andrews.distributo.lib.protocol;

public interface Marshallable {

    /**
     * Marshall the object into binary data.
     * @return the relevant binary representation associated with the instance of type T
     * @throws MarshallException if the input instance cannot produce a data representation
     */
    byte[] marshall() throws MarshallException;

    /**
     * Unmarshall binary data into a valid instance of the object.
     * @param data binary data to unmarshall
     * @throws UnmarshallException if the input data cannot produce an instance of type {@link T}
     */
    void unmarshall(byte[] data) throws UnmarshallException;
}
