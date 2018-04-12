package com.identos.smarthomecare_app;

/**
 * Created by noahz on 15.03.2018.
 */

public class BeaconEvent {
    public long startTime;
    public String beacAdd;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getBeacAdd() {
        return beacAdd;
    }

    public void setBeacAdd(String beacAdd) {
        this.beacAdd = beacAdd;
    }
}
