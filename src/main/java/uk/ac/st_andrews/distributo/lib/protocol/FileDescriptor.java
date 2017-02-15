package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * The metadata associated with a distributed file.
 */
public class FileDescriptor {

    /**
     * The path relative to the user's shared root.
     */
    public String path;

    /**
     * The unix time stamp representing the time file was revealed to distributo.
     */
    public long dateRevealed;

    public CompressionType compressionType;

    /**
     * The total size of the file taking compression into consideration.
     */
    public long totalSize;
}
