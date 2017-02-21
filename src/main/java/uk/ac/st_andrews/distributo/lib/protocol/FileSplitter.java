package uk.ac.st_andrews.distributo.lib.protocol;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * An object to aid splitting a file into chunks; as well as the buffering and loading of chunks into memory.
 */
public class FileSplitter {

    private RandomAccessFile raf;

    private long numpackets;
    private long currentPacket;

    /**
     *
     * @param file
     */
    public FileSplitter(File file) throws IOException {
        raf = new RandomAccessFile(file, "r");
        long size = file.length();
        numpackets = Packet.requiredPackets(size);
        System.out.printf("FILE INFO:\n- file size: %s\n- required packets: %s\n", size, numpackets);
    }

    public Packet nextDataPacket() throws IOException {
        if (++currentPacket == numpackets)
            currentPacket = 0;
        long pos = currentPacket * Packet.MAX_DATA_SIZE;
        raf.seek(pos);
        long size = raf.length();
        long delta = size - pos;
        System.out.printf("file pointer pos %s | size: %s\n", pos, size);
        System.out.println("reading " + delta + " bytes from file");
        //clamp the amount of data to read to the maximum packet data size
        if (delta > Packet.MAX_DATA_SIZE)
            delta = Packet.MAX_DATA_SIZE;
        byte[] data = new byte[(int)delta];
        int length = raf.read(data);
        if (length != delta)
            throw new IOException("could not read expected length " + delta + " from file, read " + length + " instead");
        //construct and return the data packet
        return Packet.makeDataPacket(currentPacket, data);
    }
}
