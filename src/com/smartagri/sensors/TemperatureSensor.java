package com.smartagri.sensors;

// Temperature Sensor
public class TemperatureSensor extends Sensor {

    private double minTemp;
    private double maxTemp;

    public TemperatureSensor(String id) {
        this(id, 5.0, 45.0); // Calls the other constructor
    }


    public TemperatureSensor(String id, double minTemp, double maxTemp) {
        super(id, "Temperature"); // Call the parent constructor
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    //generates value between max and min temp
    protected double generateNewValue() {
        // e.g., 5.0 + ( (45.0 - 5.0) * (a random value between 0.0 and 1.0) )
        return minTemp + (maxTemp - minTemp) * Math.random();
    }
}