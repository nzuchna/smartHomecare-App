package com.identos.smarthomecare_app;

import java.sql.Time;
import java.sql.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by noahz on 13.02.2018.
 */

public class Timestamp extends RealmObject {

    @PrimaryKey
    private int id;
    private int Employee_ID;
    private int Client_ID;
    private Date date;
    private Time arrival;
    private Time departure;

    public Timestamp(int employee_ID, int client_ID, Date date, Time arrival, Time departure) {
        Employee_ID = employee_ID;
        Client_ID = client_ID;
        this.date = date;
        this.arrival = arrival;
        this.departure = departure;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getArrival() {
        return arrival;
    }

    public void setArrival(Time arrival) {
        this.arrival = arrival;
    }

    public Time getDeparture() {
        return departure;
    }

    public void setDeparture(Time departure) {
        this.departure = departure;
    }
}
