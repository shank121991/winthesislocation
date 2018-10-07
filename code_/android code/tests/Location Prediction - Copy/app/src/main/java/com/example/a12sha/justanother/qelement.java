package com.example.a12sha.justanother;

import java.util.Comparator;
import java.util.Objects;

class qelement implements Comparable<qelement> {
    public double stateid;
    public double prob;
    public int hour;
    public double from_state;
    public double from_prob;
    public int from_hour;
    public int path_id;



    public qelement(double stateid, double prob, int hour, double from_state, double from_prob, int from_hour) {
        this.stateid = stateid;
        this.prob = prob;
        this.hour = hour;
        this.from_state = from_state;
        this.from_prob = from_prob;
        this.from_hour = from_hour;
    }

    public int getPath_id() {
        return path_id;
    }

    public void setPath_id(int path_id) {
        this.path_id = path_id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getFrom_state() {
        return from_state;
    }

    public void setFrom_state(double from_state) {
        this.from_state = from_state;
    }

    public double getFrom_prob() {
        return from_prob;
    }

    public void setFrom_prob(double from_prob) {
        this.from_prob = from_prob;
    }

    public int getFrom_hour() {
        return from_hour;
    }

    public void setFrom_hour(int from_hour) {
        this.from_hour = from_hour;
    }

    public double getStateid() {
        return stateid;
    }

    public void setStateid(double stateid) {
        this.stateid = stateid;
    }

    public double getProb() {
        return prob;
    }

    public void setProb(double prob) {
        this.prob = prob;
    }

    @Override
    public int compareTo(qelement employee) {
        if(this.getProb() < employee.getProb()) {
            return 1;
        } else if (this.getProb() >
                employee.getProb()) {
            return -1;
        } else {
            return 0;
        }
    }
}
