package com.identos.smarthomecare_app;

import android.util.Log;

import java.sql.Time;
import java.sql.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahz on 13.02.2018.
 */

public class Timestamp extends RealmObject {

    final static String TAG = "Timestamp";

    @PrimaryKey
    private int id;
    private int Employee_ID;
    private int Client_ID;
    private String arrival;
    private String departure;

    public Timestamp(int employee_ID, int client_ID, String arrival, String departure) {
        Employee_ID = employee_ID;
        Client_ID = client_ID;
        this.arrival = arrival;
        this.departure = departure;
    }

    public Timestamp() {
    }

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

    public int getClient_ID() {
        return Client_ID;
    }

    public void setClient_ID(int client_ID) {
        Client_ID = client_ID;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public String getDeparture() {
        return departure;
    }

    public void setDeparture(String departure) {
        this.departure = departure;
    }
}
