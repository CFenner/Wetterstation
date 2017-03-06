package de.whw.anemo.model;

import java.time.Instant;

public class SensorData {

    private long    id;
    private String  sensorName;
    private Instant timestamp;
    private double  value;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSensorName() {
        return this.sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public Instant getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SensorData [id=");
        builder.append(this.id);
        builder.append(", sensorName=");
        builder.append(this.sensorName);
        builder.append(", timestamp=");
        builder.append(this.timestamp);
        builder.append(", value=");
        builder.append(this.value);
        builder.append("]");
        return builder.toString();
    }

}
