package uk.ac.st_andrews.distributo.lib.protocol;

/**
 * Simple data type to represent a range.
 */
public class PacketRange {

    public long from;
    public long to;

    /**
     * Create a range, closed on the left and right: [from,to]
     * @param from the lower bound on the closed interval
     * @param to the upper bound on the closed interval
     * @throws IllegalArgumentException if the resulting range would be negative
     */
    public PacketRange(long from, long to) throws IllegalArgumentException {
        if (from > to)
            throw new IllegalArgumentException("cannot create a negative range");
        this.from = from;
        this.to = to;
    }

    /**
     * @param n a value to check if in this range
     * @return true if n is within this range
     */
    public boolean inRange(long n) {
        return n >= from && n <= to;
    }

    /**
     * @return the difference between the upper and lower bounds of this range
     */
    public long difference() {
        return to - from;
    }

    /**
     * @return the number of elements this range can contain.
     */
    public long size() {
        return difference() + 1;
    }

    @Override
    public String toString() {
        return "[" + from + "," + to + "]";
    }
}
