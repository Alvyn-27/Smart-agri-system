package com.smartagri.irrigation_shade;

import com.smartagri.core.FileLogger;

// The irrigation system (sprinkler control)
// This implements both Actuator and Loggable interfaces
public class IrrigationSystem implements Actuator, Loggable {
    private String id;
    private boolean isOn;  // Is the system currently on or off
    private FileLogger logger = new FileLogger();

    public IrrigationSystem(String id) {
        this.id = id;
        this.isOn = false; // Start with system off
    }

    // Turn on the irrigation
    public void activate() {
        if (!isOn) {
            isOn = true;
            logger.logEvent("Irrigation", "System " + id + " activated.");
        }
    }

    // Turn off the irrigation
    public void deactivate() {
        if (isOn) {
            isOn = false;
            logger.logEvent("Irrigation", "System " + id + " deactivated.");
        }
    }

    // Report whether it's on or off
    public String getStatus() {
        return id + " is " + (isOn ? "ON" : "OFF");
    }

    // Get the system ID
    public String getId() {
        return id;
    }

    // Provide data for logging
    public String getLogData() {
        return "IrrigationSystem [id=" + id + ", isOn=" + isOn + "]";
    }
}