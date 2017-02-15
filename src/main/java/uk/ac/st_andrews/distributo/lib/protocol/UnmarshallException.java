package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * To be thrown when there was an error in unmarshalling binary data into a {@link Marshallable} type
 */
public class UnmarshallException extends Exception {

    public UnmarshallException(String message) {
        super(message);
    }
}
