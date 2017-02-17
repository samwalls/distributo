package uk.ac.st_andrews.distributo.lib;

import uk.ac.st_andrews.distributo.lib.Marshallable;

/**
 * To be thrown when there was an error in marshalling a {@link Marshallable} type into binary data.
 */
public class MarshalException extends Exception {

    public MarshalException(String message) {
        super(message);
    }
}
