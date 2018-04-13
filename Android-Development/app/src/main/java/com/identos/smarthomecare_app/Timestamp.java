package com.identos.smarthomecare_app;

import android.util.Log;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahz on 13.02.2018.
 */

public class Timestamp extends RealmObject {

    final static String TAG = "Timestamp";

    private int Employee_ID;
    private String Client_ID;
    private long arrival;
    private long departure;
    private boolean Recording;

    public boolean addTimestamp(long starttime, int client_ID) {
        Log.i(TAG, "Starttime:" + starttime + " Client:" + client_ID);
        return true;
    }

    public int getEmployee_ID() {
        return Employee_ID;
    }

    public void setEmployee_ID(int employee_ID) {
        Employee_ID = employee_ID;
    }

    public String getClient_ID() {
        return Client_ID;
    }

    public void setClient_ID(String client_ID) {
        Client_ID = client_ID;
    }

    public long getArrival() {
        return arrival;
    }

    public void setArrival(long arrival) {
        this.arrival = arrival;
    }

    public long getDeparture() {
        return departure;
    }

    public void setDeparture(long departure) {
        this.departure = departure;
    }

    public boolean isRecording() {
        return Recording;
    }

    public void setRecording(boolean recording) {
        Recording = recording;
    }
}
