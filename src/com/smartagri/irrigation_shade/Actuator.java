package com.smartagri.irrigation_shade;

// Interface for any device that can be controlled in the field
// Any class that controls something must implement this
public interface Actuator {
    void activate();       // Turn the device on
    void deactivate();     // Turn the device off
    String getStatus();    // Get current status (ON/OFF)
    String getId();        // Get the device ID
}