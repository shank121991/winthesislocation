package com.example.a12sha.justanother;

import java.util.List;

public class markovchain {
    private String avg_lat;
    private String avg_lon;
    private String state_id;
    private String hour;
    private List<String> hour_prob;

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public List<String> getHour_prob() {
        return hour_prob;
    }

    public void setHour_prob(List<String> hour_prob) {
        this.hour_prob = hour_prob;
    }

    public String getAvg_lat() {
        return avg_lat;
    }

    public void setAvg_lat(String avg_lat) {
        this.avg_lat = avg_lat;
    }

    public String getAvg_lon() {
        return avg_lon;
    }

    public void setAvg_lon(String avg_lon) {
        this.avg_lon = avg_lon;
    }

    public String getState_id() {
        return state_id;
    }

    public void setState_id(String state_id) {
        this.state_id = state_id;
    }
}
