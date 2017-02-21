package uk.ac.st_andrews.distributo.lib.protocol;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileMerger {

    private long numPackets;
    private List<PacketRange> missing;

    private RandomAccessFile raf;

    private String relativeWritePath;
    private long writeLength;

    /**
     * @param root a directory, this machines distributo sharing root
     * @param writeInfo information about the file to merge, relative to the root
     */
    public FileMerger(File root, FileInfo writeInfo) throws IOException {
        if (root == null)
            throw new IOException("cannot create file merger on a null file root");
        else if (!root.exists())
            throw new IOException(root.getPath() + " does not exist");
        else if (!root.isDirectory())
            throw new IOException("cannot create file merger on a non-directory");
        this.relativeWritePath = writeInfo.relativePath();
        this.writeLength = writeInfo.fileLength();
        this.numPackets = Packet.requiredPackets(writeLength);
        missing = new ArrayList<>();
        //to start with we have packets missing in the range 0 to (L - 1), where L is the total number of required packets
        missing.add(new PacketRange(0, numPackets - 1));
        String fullPath = root.getAbsolutePath() + System.getProperty("file.separator") + relativeWritePath;
        raf = new RandomAccessFile(fullPath, "rw");
        System.out.printf("creating file:\n- share root: %s\n- sent path: %s\n- written file path: %s\n",
                root.getAbsolutePath(),
                relativeWritePath,
                fullPath
        );
    }

    public void writePacket(Packet dataPacket) throws IllegalArgumentException, IOException {
        if (dataPacket == null || dataPacket.data() == null)
            throw new IllegalArgumentException("cannot write null data packet contents to file");
        else if (dataPacket.data().length == 0)
            throw new IllegalArgumentException("cannot write empty data packet contents to file");
        else if (dataPacket.type() != PacketType.DATA)
            throw new IllegalArgumentException("cannot write non-data packet contents to file");
        //write contents to the file
        long packetno = dataPacket.packetno();
        long pos = Packet.MAX_DATA_SIZE * packetno;
        raf.seek(pos);
        raf.write(dataPacket.data());
        //adjust the range of missing packets
        adjustRange(dataPacket.packetno());
    }

    /**
     * Based on the assumption that the packet with the number of the passed argument is no longer missing, we need to
     * update the ranges of missing packets.
     * @param received the number of the received packet
     */
    private void adjustRange(long received) {
        for (int i = 0; i < missing.size(); i++) {
            PacketRange range = missing.get(i);
            if (range.inRange(received)) {
                if (range.from == received) {
                    //check the lower limit
                    range.from++;
                    if (range.difference() <= 0)
                        missing.remove(i);
                } else if (range.to == received) {
                    //check the upper limit
                    range.to--;
                    if (range.difference() <= 0)
                        missing.remove(i);
                } else {
                    //the received item is in the middle of the range, we need to split the range into two
                    PacketRange lower = new PacketRange(range.from, received - 1);
                    range.from = received + 1;
                    missing.add(i, lower);
                    /*
                    e.g.
                    before: [3,9]
                    received: 5
                    after [3,4], [6,9]
                     */
                }
            }
        }
    }
}