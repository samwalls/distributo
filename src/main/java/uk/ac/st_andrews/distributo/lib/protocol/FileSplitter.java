package uk.ac.st_andrews.distributo.lib.protocol;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * An object to aid splitting a file into chunks; as well as the buffering and loading of chunks into memory.
 */
public class FileSplitter {

    /**
     * The number of bytes per chunk.
     */
    //10KB per chunk
    private static long BYTES_PER_CHUNK = 10000;

    private static long PACKETS_PER_CHUNK = 10;

    private static long asd;

    private File file;

    private PriorityQueue<Integer> chunkQueue;

    private Map<Integer, byte[]> chunks;

    /**
     *
     * @param file
     */
    public FileSplitter(File file) {
        this.file = file;
        chunks = new HashMap<>();
        chunkQueue = new PriorityQueue<>((a, b) -> {
            return 1;
        });
    }

    public Packet getDataPacket(long chunk) {
        return null;
    }
}
