package uk.ac.st_andrews.distributo.lib.protocol;

import uk.ac.st_andrews.distributo.lib.MarshalException;
import uk.ac.st_andrews.distributo.lib.Marshallable;
import uk.ac.st_andrews.distributo.lib.UnmarshalException;

import java.util.Arrays;

/**
 * Marshallable type to be used within a {@link Packet} payload.
 */
public class FileInfo implements Marshallable {

    /**
     * The length of the file that the receivers should get in bytes.
     */
    private long _fileLength;

    /**
     * The relative path from the receiver's sharing root of the received file-to-be.
     */
    private String _relativePath;

    public FileInfo(long fileLength, String relativePath) {
        this._fileLength = fileLength;
        this._relativePath = relativePath;
    }

    public FileInfo() {
        this(0, null);
    }

    @Override
    public byte[] marshal() throws MarshalException {
        byte[] bytes = Packet.getBytes(_fileLength);
        bytes = Packet.merge(bytes, _relativePath.getBytes());
        return bytes;
    }

    @Override
    public void unmarshal(byte[] data) throws UnmarshalException {
        byte[] fileLengthBytes = Arrays.copyOfRange(data, 0, Long.BYTES);
        long fileLength = Packet.getLong(fileLengthBytes);
        String relativePath = new String(Arrays.copyOfRange(data, Long.BYTES, data.length));
        //set this object with the data
        this._fileLength = fileLength;
        this._relativePath = relativePath;
    }

    @Override
    public String toString() {
        return "{ length: " + _fileLength + "; path: " + _relativePath + "}";
    }

    public long fileLength() {
        return _fileLength;
    }

    public String relativePath() {
        return _relativePath;
    }
}
