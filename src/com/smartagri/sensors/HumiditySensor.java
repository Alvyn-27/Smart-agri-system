package com.smartagri.sensors;

//Humidity Sensor
public class HumiditySensor extends Sensor {

    public HumiditySensor(String id) {
        super(id, "Humidity"); //parent constructor
    }

    // generates random percentage between 30 to 90
    protected double generateNewValue() {
        // 30.0 + ( (a random value between 0.0 and 1.0) * 60.0 )
        return 30.0 + (60.0 * Math.random());
    }
}