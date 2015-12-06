package com.sevenstringargs.plantit.plantit.model;

import java.util.Date;

/**
 * Created by Robban on 05/12/15.
 */
public class SensorData {
    private String deviceId;
    private Date date;
    private double value;

    public SensorData(String deviceId, Date date, String value){
        this.deviceId = deviceId;
        this.date = date;
        this.value = Double.valueOf(value);
    }

    public String getDeviceId(){
        return deviceId;
    }

    public Date getDate(){
        return date;
    }

    public double getValue(){
        return value;
    }
}
