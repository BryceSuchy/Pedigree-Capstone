package com.pedigreetechnologies.diagnosticview;

/**
 * Created by Joe on 4/5/2017.
 * An object to hold the data points after the messages are processed, these are used to add data to the graphs
 */

public class SensorDataPoints {

    private float dataPoint;
    private long time;

    public SensorDataPoints(float dataPoint, long time) {
        this.dataPoint = dataPoint;
        this.time = time;
    }

    public float getDataPoint() {
        return dataPoint;
    }

    public long getTime() {
        return time;
    }

    public String toString(){
        return "[" + dataPoint + "]" + "{" + time + "}";
    }

}
