package uk.ac.st_andrews.distributo.lib;

import uk.ac.st_andrews.distributo.lib.Marshallable;

/**
 * To be thrown when there was an error in unmarshalling binary data into a {@link Marshallable} type
 */
public class UnmarshalException extends Exception {

    public UnmarshalException(String message) {
        super(message);
    }
}
