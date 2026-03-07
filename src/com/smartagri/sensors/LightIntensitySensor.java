package com.smartagri.sensors;

//Light Intensity Sensor.
public class LightIntensitySensor extends Sensor {

    public LightIntensitySensor(String id) {
        super(id, "Light Intensity"); // Call the parent constructor
    }

    // generates random value between 1000 to 50000
    protected double generateNewValue() {
        // 1000.0 + ( (a random value between 0.0 and 1.0) * 49000.0 )
        return 1000.0 + (49000.0 * Math.random());
    }
}