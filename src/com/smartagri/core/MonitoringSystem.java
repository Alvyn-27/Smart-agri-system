package com.smartagri.core;

import com.smartagri.irrigation_shade.IrrigationSystem;
import com.smartagri.irrigation_shade.ShadeSystem;
import com.smartagri.irrigation_shade.Actuator;
import com.smartagri.sensors.Sensor;
import com.smartagri.sensors.SoilMoistureSensor;
import com.smartagri.sensors.TemperatureSensor;

// Controls sensors, actuators, and automation logic
public class MonitoringSystem {

    // Settings for automation features
    public static class AutomationConfig {
        private String configName;
        private boolean enabled;
        private int priority;

        public AutomationConfig(String configName, boolean enabled, int priority) {
            this.configName = configName;
            this.enabled = enabled;
            this.priority = priority;
        }
        public String getConfigName() { return configName; }
        public boolean isEnabled() { return enabled; }
        public int getPriority() { return priority; }
        
        // Returns config details as text
        public String toString() {
            return "AutomationConfig{" + "name='" + configName + '\'' +
                   ", enabled=" + enabled + ", priority=" + priority + '}';
        }
    }

    // Stores a key-value pair for thresholds
    private static class ThresholdEntry {
        String key;
        Double value;
        ThresholdEntry(String key, Double value) {
            this.key = key;
            this.value = value;
        }
    }

    // System components and state
    private Sensor[] sensors;
    private int sensorCount;
    private Actuator[] actuators;
    private int actuatorCount;
    private ThresholdEntry[] thresholds;
    private int thresholdCount;
    private FileLogger logger;
    private AutomationConfig automationConfig;

    // Setup system with defaults
    public MonitoringSystem() {
        this.sensors = new Sensor[20];
        this.sensorCount = 0;
        this.actuators = new Actuator[10];
        this.actuatorCount = 0;
        this.thresholds = new ThresholdEntry[10];
        this.thresholdCount = 0;
        this.logger = new FileLogger();
        this.automationConfig = new AutomationConfig("DEFAULT_CONFIG", true, 1);

        // Set default safety limits
        addThreshold("Moisture_Low", 30.0);
        addThreshold("Temp_High", 35.0);
    }

    // Adds a new threshold value
    private void addThreshold(String key, double value) {
        if (thresholdCount >= thresholds.length) {
            expandThresholds();
        }
        thresholds[thresholdCount] = new ThresholdEntry(key, value);
        thresholdCount++;
    }

    // Makes the threshold array bigger
    private void expandThresholds() {
        ThresholdEntry[] newArray = new ThresholdEntry[thresholds.length * 2];
        for (int i = 0; i < thresholdCount; i++) {
            newArray[i] = thresholds[i];
        }
        thresholds = newArray;
    }

    public AutomationConfig getAutomationConfig() { return automationConfig; }

    // Updates automation settings
    public void setAutomationConfig(AutomationConfig config) {
        this.automationConfig = config;
        logger.log("CONFIG", "Automation configuration updated: " + config);
    }

    // Registers a sensor
    public void addSensor(Sensor sensor) {
        if (sensorCount >= sensors.length) {
            expandSensors();
        }
        sensors[sensorCount] = sensor;
        sensorCount++;
        logger.log("SYSTEM", "Registered sensor: " + sensor.getId());
    }

    // Makes sensor array bigger
    private void expandSensors() {
        Sensor[] newArray = new Sensor[sensors.length * 2];
        for (int i = 0; i < sensorCount; i++) {
            newArray[i] = sensors[i];
        }
        sensors = newArray;
    }

    // Registers an actuator
    public void addActuator(Actuator actuator) {
        if (actuatorCount >= actuators.length) {
            expandActuators();
        }
        actuators[actuatorCount] = actuator;
        actuatorCount++;
        logger.log("SYSTEM", "Registered actuator: " + actuator.getId());
    }

    // Makes actuator array bigger
    private void expandActuators() {
        Actuator[] newArray = new Actuator[actuators.length * 2];
        for (int i = 0; i < actuatorCount; i++) {
            newArray[i] = actuators[i];
        }
        actuators = newArray;
    }

    // Updates an existing threshold
    public void setThreshold(String key, double value) {
        for (int i = 0; i < thresholdCount; i++) {
            if (thresholds[i].key.equals(key)) {
                thresholds[i].value = value;
                logger.log("CONFIG", "Threshold '" + key + "' set to " + value);
                return;
            }
        }
        throw new IllegalArgumentException("Invalid threshold key: " + key);
    }

    // Gets a threshold value
    public Double getThreshold(String key) {
        for (int i = 0; i < thresholdCount; i++) {
            if (thresholds[i].key.equals(key)) {
                return thresholds[i].value;
            }
        }
        return null;
    }

    // Formats thresholds for display
    public String getThresholdsString() {
        String result = "";
        for (int i = 0; i < thresholdCount; i++) {
            result = result + thresholds[i].key + ": " + thresholds[i].value;
            if (i < thresholdCount - 1) {
                result = result + ", ";
            }
        }
        return result;
    }

    // Returns active sensors
    public Sensor[] getSensors() {
        Sensor[] result = new Sensor[sensorCount];
        for (int i = 0; i < sensorCount; i++) {
            result[i] = sensors[i];
        }
        return result;
    }

    // Returns active actuators
    public Actuator[] getActuators() {
        Actuator[] result = new Actuator[actuatorCount];
        for (int i = 0; i < actuatorCount; i++) {
            result[i] = actuators[i];
        }
        return result;
    }

    // Type checks
    public boolean isSensor(Object obj) { return obj instanceof Sensor; }
    public boolean isActuator(Object obj) { return obj instanceof Actuator; }

    // Counts specific sensor types
    public int countSensorsOfType(Class<?> sensorType) {
        int count = 0;
        for (int i = 0; i < sensorCount; i++) {
            if (sensorType.isInstance(sensors[i])) {
                count++;
            }
        }
        return count;
    }

    // reads sensors and triggers actions
    public void checkAllConditions() {
        logger.log("MONITOR", "Running automation check");
        double currentMoisture = -1;
        double currentTemp = -1;

        // Read all sensors
        for (int i = 0; i < sensorCount; i++) {
            Sensor sensor = sensors[i];
            Sensor.SensorReading reading = sensor.read();
            
            logger.log(reading.toString());

            // Save values for rules
            if (sensor instanceof SoilMoistureSensor) {
                currentMoisture = reading.value;
            } else if (sensor instanceof TemperatureSensor) {
                currentTemp = reading.value;
            }
        }

        // Run rules
        applyIrrigationRule(currentMoisture);
        applyShadeRule(currentTemp);
    }

    // Turn on water if soil is dry
    private void applyIrrigationRule(double currentMoisture) {
        if (currentMoisture == -1) return;
        
        Double lowMoistureThreshold = getThreshold("Moisture_Low");
        if (lowMoistureThreshold == null) return; // Safety check

        IrrigationSystem irrigation = getIrrigationSystem();
        if (irrigation == null) return;

        // Check condition
        if (currentMoisture < lowMoistureThreshold) {
            logger.logEvent("AUTOMATION", String.format("Soil moisture LOW (%.1f). Activating irrigation.", currentMoisture));
            irrigation.activate();
        } else {
            irrigation.deactivate();
        }
    }

    // Deploy shades if temp is high
    private void applyShadeRule(double currentTemp) {
        if (currentTemp == -1) return;
        
        Double highTempThreshold = getThreshold("Temp_High");
        if (highTempThreshold == null) return; // Safety check

        ShadeSystem shade = getShadeSystem();
        if (shade == null) return;

        // Check condition
        if (currentTemp > highTempThreshold) {
            logger.logEvent("AUTOMATION", String.format("Temperature HIGH (%.1f). Deploying shades.", currentTemp));
            shade.activate();
        } else {
            shade.deactivate();
        }
    }

    // irrigation actuator
    public IrrigationSystem getIrrigationSystem() {
        for (int i = 0; i < actuatorCount; i++) {
            if (actuators[i] instanceof IrrigationSystem) {
                return (IrrigationSystem) actuators[i];
            }
        }
        return null;
    }

    // shade actuator
    public ShadeSystem getShadeSystem() {
        for (int i = 0; i < actuatorCount; i++) {
            if (actuators[i] instanceof ShadeSystem) {
                return (ShadeSystem) actuators[i];
            }
        }
        return null;
    }
}