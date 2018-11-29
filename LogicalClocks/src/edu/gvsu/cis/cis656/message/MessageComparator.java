package edu.gvsu.cis.cis656.message;

import edu.gvsu.cis.cis656.clock.VectorClockComparator;

import java.util.Comparator;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    private VectorClockComparator comparator = new VectorClockComparator();

    @Override
    public int compare(Message lhs, Message rhs) {
        // Write your code here
        return comparator.compare(lhs.ts, rhs.ts);
    }

}
