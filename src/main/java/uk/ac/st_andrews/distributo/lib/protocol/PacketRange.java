package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * Simple data type to represent a range.
 */
public class PacketRange {

    public long from;
    public long to;

    /**
     * Create a range, closed on the left and right: [from,to]
     * @param from
     * @param to
     * @throws IllegalArgumentException
     */
    public PacketRange(long from, long to) throws IllegalArgumentException {
        if (from > to)
            throw new IllegalArgumentException("cannot create a negative range");
        this.from = from;
        this.to = to;
    }

    /**
     * @param n
     * @return true if n is within this range
     */
    public boolean inRange(long n) {
        return n >= from && n <= to;
    }

    public long difference() {
        return to - from;
    }
}
