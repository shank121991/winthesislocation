package com.example.a12sha.justanother;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class states implements Parcelable {
    private double lat;
    private double lon;
    private double stateid;
    private String address;

    public states() {
        lat = 0;
        lon = 0;
        stateid = 0;
        address = "0";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getStateid() {
        return stateid;
    }

    public void setStateid(double stateid) {
        this.stateid = stateid;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        // TODO Auto-generated method stub
        dest.writeDouble(lat);
        dest.writeDouble(lon);
        dest.writeDouble(stateid);
        dest.writeString(address);
    }

    public states(Parcel in) {
        lat = in.readDouble();
        lon = in.readDouble();
        stateid = in.readDouble();
        address = in.readString();
    }

    public static final Parcelable.Creator<states> CREATOR = new Parcelable.Creator<states>() {
        public states createFromParcel(Parcel in) {
            return new states(in);
        }

        public states[] newArray(int size) {
            return new states[size];
        }
    };
}
