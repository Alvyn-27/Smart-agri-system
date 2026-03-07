package com.smartagri.sensors;

//Soil Moisture Sensor
public class SoilMoistureSensor extends Sensor {

    public SoilMoistureSensor(String id) {
        super(id, "Soil Moisture"); // Call the parent constructor
    }

    //generates random percentage between 10 to 70
    protected double generateNewValue() {
        // 10.0 + ( (a random value between 0.0 and 1.0) * 60.0 )
        return 10.0 + (60.0 * Math.random());
    }
}