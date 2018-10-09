package com.example.a12sha.justanother;

public class hourlyweights {

    private String date;
    private String stateid;
    private String avg_latitude;
    private String avg_longitude;
    private String[] hour_w;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStateid() {
        return stateid;
    }

    public void setStateid(String stateid) {
        this.stateid = stateid;
    }

    public String getAvg_latitude() {
        return avg_latitude;
    }

    public void setAvg_latitude(String avg_latitude) {
        this.avg_latitude = avg_latitude;
    }

    public String getAvg_longitude() {
        return avg_longitude;
    }

    public void setAvg_longitude(String avg_longitude) {
        this.avg_longitude = avg_longitude;
    }

    public String[] getHour_w() {
        return hour_w;
    }

    public void setHour_w(String[] hour_w) {
        this.hour_w = hour_w;
    }

    public String getiHour_w(int hour) {
        String hour_prob = hour_w[hour];
        return hour_prob;
    }

}
