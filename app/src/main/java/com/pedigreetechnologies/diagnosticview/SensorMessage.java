package com.pedigreetechnologies.diagnosticview;

/**
 * Created by Joe on 4/5/2017.
 * Message stored directly from the sensor
 */

public class SensorMessage{

    private String message;
    private long timeReceived;

    public SensorMessage(String message, long timeReceived){
        this.message = message;
        this.timeReceived = timeReceived;
    }

    public String getMessage() {
        return message;
    }

    public long getTimeReceived() {
        return timeReceived;
    }
}
