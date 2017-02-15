package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * To be thrown when there was an error in marshalling a {@link Marshallable} type into binary data.
 */
public class MarshallException extends Exception {

    public MarshallException(String message) {
        super(message);
    }
}
