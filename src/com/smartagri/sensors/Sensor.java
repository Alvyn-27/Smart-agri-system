package com.smartagri.sensors;

import java.time.LocalDateTime;

// sensor class
public abstract class Sensor {
    protected String id;
    protected String type;
    protected double currentValue;
    protected LocalDateTime lastReadingTime;

    public Sensor(String id, String type) {
        this.id = id;
        this.type = type;
        this.currentValue = 0.0;
        this.lastReadingTime = LocalDateTime.now();
    }

    //timestamp - value holder
    public class SensorReading {
        public final double value;
        public final LocalDateTime timestamp;

        private SensorReading(double value, LocalDateTime timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public Sensor getSensor() {
            return Sensor.this;
        }

        public String toString() {
            // Format double value with 2 decimal places
            double val = this.value;
            long rounded = Math.round(val * 100);
            double formatted = rounded / 100.0;
            // format: "Sensor T1 (Temperature): 35.85"
            return "Sensor " + getSensor().id + " (" + getSensor().type + "): " + formatted;
        }
    }

    public SensorReading read() {
        this.currentValue = generateNewValue();
        this.lastReadingTime = LocalDateTime.now();
        return new SensorReading(this.currentValue, this.lastReadingTime);
    }

    protected abstract double generateNewValue();

    // Getters
    public String getId() { return id; }
    public String getType() { return type; }
    public double getCurrentValue() { return currentValue; }
}