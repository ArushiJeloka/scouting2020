package com.stuin.irs_scout.Data;

/**
 * Created by Stuart on 2/10/2017.
 */
public class Measure {
    final public int match;
    final public int team;
    final public String task;
    final public String page;
    public String value;
    public int success;
    public int miss;

    public Measure() {
        match = 0;
        team = 0;
        task = "";
        page = "";
        value = "";
        success = 0;
        miss = 0;
    }

    public Measure(Task task, int match, int team, String phase) {
        this.match = match;
        this.team = team;
        this.task = task.task;
        this.page = phase;
        this.value = "";
        this.success = 0;
        this.miss = 0;
    }

    public boolean Equals(Measure measure) {
        if(!task.equals(measure.task)) return false;
        if(!value.equals(measure.value)) return false;
        if(success != measure.success) return false;
        if(miss != measure.miss) return false;
        return true;
    }
}
