package com.smartagri.irrigation_shade;

import com.smartagri.core.FileLogger;

// The shade system 
// Implements both Actuator and Loggable
// DEPLOYED or RETRACTED.
public class ShadeSystem implements Actuator, Loggable {

    private String id;
    private boolean isDeployed;// Are the shades out or in
    private FileLogger logger = new FileLogger();

    // Constructor sets up the shade system
    public ShadeSystem(String id) {
        this.id = id;
        this.isDeployed = false;// Shades start retracted
    }

    // Deploy the shades (open them)
    public void activate() {
        if (!isDeployed) {
            isDeployed = true;
            logger.logEvent("SHADE_SYSTEM", "Shades " + id + " on.");
        }
    }

    // Retract the shades (close them)
    public void deactivate() {
        if (isDeployed) {
            isDeployed = false;
            logger.logEvent("SHADE_SYSTEM", "Shades " + id + " off.");
        }
    }

    public String getStatus() {
        // Simplified status
        return id + " is " + (isDeployed ? "ON" : "OFF");
    }

    // Get the system ID
    public String getId() {
        return id;
    }

    // Provide data for logging
    public String getLogData() {
        // Simplified log data
        return "ShadeSystem [id=" + id + ", isDeployed=" + isDeployed + "]";
    }
}