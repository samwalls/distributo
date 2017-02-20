package uk.ac.st_andrews.distributo.lib;

import java.io.IOException;

/**
 * To be thrown when there was an error in marshalling a {@link Marshallable} type into binary data.
 */
public class MarshalException extends IOException {

    public MarshalException(String message) {
        super(message);
    }
}
