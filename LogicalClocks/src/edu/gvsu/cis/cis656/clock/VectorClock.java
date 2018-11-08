package edu.gvsu.cis.cis656.clock;

import java.util.Hashtable;
import java.util.Map;

public class VectorClock implements Clock {

    // suggested data structure ...
    private Map<String,Integer> clock = new Hashtable<String,Integer>();


    @Override
    public void update(Clock other) {

    }

    @Override
    public void setClock(Clock other) {

    }

    @Override
    public void tick(Integer pid) {

    }

    @Override
    public boolean happenedBefore(Clock other) {
        return false;
    }

    public String toString() {
        // TODO: you implement
        return null;
    }

    @Override
    public void setClockFromString(String clock) {

    }

    @Override
    public int getTime(int p) {
        return 0;
    }

    @Override
    public void addProcess(int p, int c) {

    }
}
