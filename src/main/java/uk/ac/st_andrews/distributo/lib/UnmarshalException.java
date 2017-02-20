package uk.ac.st_andrews.distributo.lib;

import java.io.IOException;

/**
 * To be thrown when there was an error in unmarshalling binary data into a {@link Marshallable} type
 */
public class UnmarshalException extends IOException {

    public UnmarshalException(String message) {
        super(message);
    }
}
