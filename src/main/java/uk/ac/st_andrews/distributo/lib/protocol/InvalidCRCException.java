package uk.ac.st_andrews.distributo.lib.protocol;

import uk.ac.st_andrews.distributo.lib.UnmarshalException;

/**
 * To be thrown during unmarshalling, if the CRC checksum of the unmarshalled packet does not match its checksum field.
 */
public class InvalidCRCException extends UnmarshalException {

    public InvalidCRCException(String message) {
        super(message);
    }
}
