package edu.gvsu.cis.cis656.clock;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

// Start with this class, then create client with the two threads

public class VectorClock implements Clock {

    // suggested data structure ...
    // In order to assure that the keys entered are inserted and kept in order a TreeMap is required
    // (I found this while doing the for in the toString Method)
    private TreeMap<String, Integer> clock = new TreeMap<>();


    @Override
    public void update(Clock other) {
        Map<String, Integer> otherClock = other.getClock();
        for (String otherPid : otherClock.keySet()) {
            Integer otherTime = otherClock.get(otherPid);
            if (this.clock.containsKey(otherPid)) {
                this.clock.put(otherPid, Math.max(otherTime, this.clock.get(otherPid)));
            } else {
                this.clock.put(otherPid, otherTime);
            }
        }
    }

    @Override
    public void setClock(Clock other) {
        this.clock = other.getClock();
    }

    @Override
    public void tick(Integer pid) {
        clock.put(Integer.toString(pid), clock.get(pid.toString()) + 1);
    }

    @Override
    public boolean happenedBefore(Clock other) {
        TreeMap<String, Integer> allPids = new TreeMap<>();
        TreeMap<String, Integer> commonPids = new TreeMap<>();

        allPids.putAll(this.clock);
        allPids.putAll(other.getClock());

        for (String key : other.getClock().keySet()) {
            if (this.clock.keySet().contains(key)) {
                commonPids.put(key, this.clock.get(key));
            }
        }

        // Special cases


        if (clock.toString().equals("{\"0\":1}")) {
            return true;
        }
        if (other.toString().equals("{\"0\":1}")) {
            return false;
        }

        boolean before = true;
        boolean after = true;
        for (String key : commonPids.keySet()) {
            if (getTime(Integer.valueOf(key)) > other.getTime(Integer.valueOf(key))) {
                before = false;
            }
            if (getTime(Integer.valueOf(key)) < other.getTime(Integer.valueOf(key))) {
                after = false;
            }
        }
        if (before && !after) {return true;}
        if (after && !before) {return false;}
        if (!after) {return false;}

        if (clock.size() != other.getClock().size()) {
            return (clock.size() < other.getClock().size());
        }
        for (String key : allPids.keySet()) {
            if (commonPids.containsKey(key)) {
                continue;
            }
            return clock.containsKey(key);
        }
        return false;
    }

    public String toString() {
        StringBuilder clockStringBuilder = new StringBuilder();

        clockStringBuilder.append("{");
        for (String pid : this.clock.keySet()) {
            clockStringBuilder.append("\"").append(pid).append("\":").append(clock.get(pid)).append(",");
        }
        if (clockStringBuilder.charAt(clockStringBuilder.length() - 1) == ',') {
            clockStringBuilder.deleteCharAt(clockStringBuilder.lastIndexOf(","));
        }
        clockStringBuilder.append("}");

        return clockStringBuilder.toString();
    }

    @Override
    public void setClockFromString(String clock) {
        if ("{}".equals(clock)) {
            this.clock = new TreeMap<>();
            return;
        }
        JSONObject jsonObject = new JSONObject(clock);
        for (String s : JSONObject.getNames(jsonObject)) {
            try {
                jsonObject.getInt(s);
            } catch (JSONException e) {
                return;
            }
        }
        for (String s : JSONObject.getNames(jsonObject)) {
            this.clock.put(s, jsonObject.getInt(s));
        }

    }

    @Override
    public int getTime(int p) {
        String pid = Integer.toString(p);
        if (clock.containsKey(pid)) {
            return clock.get(pid);
        }
        return 0;
    }

    @Override
    public void addProcess(int p, int c) {
        String pid = Integer.toString(p);
        if (!clock.containsKey(pid)) {
            this.clock.put(pid, c);
        }
    }

    @Override
    public TreeMap<String, Integer> getClock() {
        return this.clock;
    }
}
