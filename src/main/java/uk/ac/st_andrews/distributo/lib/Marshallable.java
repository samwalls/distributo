package uk.ac.st_andrews.distributo.lib;

/**
 * Describes objects that can <em>marshal</em> into a binary form, and <em>unmarshal</em> back into an object of the
 * same type. Caution, the implementing class should implement a basic constructor such that the following paradigm can
 * be followed to unmarshal:<br>
 * <pre>
 *     byte[] data = myObject.marshal();
 *     //note the use of an empty constructor
 *     MyObject newObject = new MyObject();
 *     //unmarshalling with the byte array should make newObject the same as myObject
 *     newObject.unmarshal(data);
 * </pre>
 */
public interface Marshallable {

    /**
     * Marshal the object into binary data.
     * @return the relevant binary representation associated with the implementing instance
     * @throws MarshalException if the input instance cannot produce a data representation
     */
    byte[] marshal() throws MarshalException;

    /**
     * Unmarshal binary data into a valid instance of the object.
     * @param data binary data to unmarshal
     * @throws UnmarshalException if the input data cannot produce an implementing instance
     */
    void unmarshal(byte[] data) throws UnmarshalException;
}
