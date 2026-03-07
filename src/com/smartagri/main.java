package com.smartagri;

import com.smartagri.irrigation_shade.Actuator;
import com.smartagri.irrigation_shade.IrrigationSystem;
import com.smartagri.irrigation_shade.ShadeSystem;
import com.smartagri.core.FileLogger;
import com.smartagri.core.MonitoringSystem;
import com.smartagri.sensors.*;
import com.smartagri.users.AuthenticationException;
import com.smartagri.users.AuthenticationService;
import com.smartagri.users.User;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

// main cli
// sensors, actuators, and handles user authentication
// menus for different user roles (admin, agronomist, farmer)
public class main {

    // System components
    private static AuthenticationService authService = new AuthenticationService();
    private static MonitoringSystem monitoringSystem = new MonitoringSystem();
    private static FileLogger logger = new FileLogger();
    private static User currentUser = null;
    private static Scanner scanner = new Scanner(System.in); // For user input
    private static Timer sensorTimer; // For sensor simulation

    public static void main(String[] args) {
        initializeSystem();
        runLoginLoop();
        shutdownSystem();
    }

    //add sensors and actuators to the system
    private static void initializeSystem() {
        logger.log("SYSTEM", "Smart Agriculture System Initializing...");
        
        // Add Sensors
        monitoringSystem.addSensor(new TemperatureSensor("T1", 10.0, 40.0));
        monitoringSystem.addSensor(new SoilMoistureSensor("M1"));
        monitoringSystem.addSensor(new HumiditySensor("H1"));
        monitoringSystem.addSensor(new LightIntensitySensor("L1"));

        // Add Actuators
        monitoringSystem.addActuator(new IrrigationSystem("IRR-MAIN"));
        monitoringSystem.addActuator(new ShadeSystem("SHD-MAIN"));
        
        logger.log("SYSTEM", "Initialization complete.");
    }

    // Keep asking for login until user exits
    private static void runLoginLoop() {
        while (true) {
            System.out.println("\nSmart Agriculture Login");
            System.out.println("1.Login");
            System.out.println("2.Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();
            if (choice.equals("1")) {
                handleLogin();
                if (currentUser != null) {
                    startSimulation(); // Start background updates
                    runUserMenu();     // Show user-specific menu
                    stopSimulation();  // Stop updates on logout
                }
            } else if (choice.equals("2")) {
                break; // Exit
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }

    // log in the user with username and password
    private static void handleLogin() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();

            currentUser = authService.login(username, password);
            logger.log("AUTH", "User '" + currentUser.getUsername() + "' logged in successfully.");
            System.out.println("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        
        } catch (AuthenticationException e) { // exception check for login failure
            logger.log("AUTH_FAIL", e.getMessage());
            System.out.println(e.getMessage());
            
            // show error type
            if (e.isUserNotFound()) {
                System.out.println("  -> Username does not exist.");
            } else if (e.isPasswordIncorrect()) {
                System.out.println("  -> Password is incorrect.");
            }
            
            currentUser = null;
        }
    }
	// Tried to implement no echo password
	/*
	    private static void handleLogin() {
        try {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password;
            if (console != null) {
                password = new String(console.readPassword());
            } else {
                // fallback for IDEs where console is null
                password = scanner.nextLine();
            }

            currentUser = authService.login(username, password);
            logger.log("AUTH", "User '" + currentUser.getUsername() + "' logged in successfully.");
            System.out.println("Welcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        
        } catch (AuthenticationException e) { // exception check for login failure
            logger.log("AUTH_FAIL", e.getMessage());
            System.out.println(e.getMessage());
            
            // show error type
            if (e.isUserNotFound()) {
                System.out.println("  -> Username does not exist.");
            } else if (e.isPasswordIncorrect()) {
                System.out.println("  -> Password is incorrect.");
            }
            
            currentUser = null;
        }
    }
	*/
    // timer start, that updates sensors in the background
    private static void startSimulation() {
        sensorTimer = new Timer();
        TimerTask simulationTask = new TimerTask() {
            public void run() {
                // This runs in a separate thread
                logger.log("Simulation", "Running periodic sensor scan and automation");
                monitoringSystem.checkAllConditions();
            }
        };
        // runs every 10 seconds
        sensorTimer.schedule(simulationTask, 5000, 10000); 
    }

    // Stop the background sensor timer
    private static void stopSimulation() {
        if (sensorTimer != null) {
            sensorTimer.cancel();
            sensorTimer = null;
            logger.log("Simulation", "Simulation stopped.");
        }
    }

    // menu based on users role (admin, agro or farmer)
    private static void runUserMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            // Get the user role
            String userRole = currentUser.getRole();
            
            // menu based on role
            if (userRole.equals("ADMIN")) {
                loggedIn = showAdminMenu();
            } else if (userRole.equals("AGRONOMIST")) {
                loggedIn = showAgronomistMenu();
            } else if (userRole.equals("FARMER")) {
                loggedIn = showFarmerMenu();
            }
        }
        currentUser = null; // logging out
        System.out.println("You have been logged out.");
    }

    // MENUS

    private static boolean showAdminMenu() {
        System.out.println("\n[Admin Menu]");
        System.out.println("1.View All Sensor Data");
        System.out.println("2.View All Actuator Status");
        System.out.println("3.View System Thresholds");
        System.out.println("4.Set New Threshold");
        System.out.println("5.Logout");
        System.out.print("Choose option: ");
        
        String choice = scanner.nextLine();
        if (choice.equals("1")) {
            viewAllSensorData();
        } else if (choice.equals("2")) {
            viewAllActuatorStatus();
        } else if (choice.equals("3")) {
            viewThresholds();
        } else if (choice.equals("4")) {
            setThreshold();
        } else if (choice.equals("5")) {
            return false; // Logout
        } else {
            System.out.println("Invalid option.");
        }
        return true; // Stay logged in
    }

    private static boolean showAgronomistMenu() {
        System.out.println("\n[Agronomist Menu]");
        System.out.println("1.View All Sensor Data");
        System.out.println("2.View System Thresholds");
        System.out.println("3.Run Manual System Check");
        System.out.println("4.Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        if (choice.equals("1")) {
            viewAllSensorData();
        } else if (choice.equals("2")) {
            viewThresholds();
        } else if (choice.equals("3")) {
            System.out.println("Running manual system check...");
            monitoringSystem.checkAllConditions();
        } else if (choice.equals("4")) {
            return false; // Logout
        } else {
            System.out.println("Invalid option.");
        }
        return true; // Stay logged in
    }

    private static boolean showFarmerMenu() {
        System.out.println("\n[Farmer Menu]");
        System.out.println("1.View My Sensor Data");
        System.out.println("2.View Actuator Status");
        System.out.println("3.Manually Activate Irrigation");
        System.out.println("4.Manually Deactivate Irrigation");
        System.out.println("5.Logout");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine();
        if (choice.equals("1")) {
            viewAllSensorData();
        } else if (choice.equals("2")) {
            viewAllActuatorStatus();
        } else if (choice.equals("3")) {
            IrrigationSystem irr = monitoringSystem.getIrrigationSystem();
            if (irr != null) irr.activate();
        } else if (choice.equals("4")) {
            IrrigationSystem irr2 = monitoringSystem.getIrrigationSystem();
            if (irr2 != null) irr2.deactivate();
        } else if (choice.equals("5")) {
            return false; // Logout
        } else {
            System.out.println("Invalid option.");
        }
        return true; // Stay logged in
    }

    // Actions

    private static void viewAllSensorData() {
        System.out.println("\nCurrent Sensor Readings");
        Sensor[] allSensors = monitoringSystem.getSensors();
        for (int i = 0; i < allSensors.length; i++) {
            Sensor s = allSensors[i];
            // decimal output to 2 places
            double value = s.getCurrentValue();
            long rounded = Math.round(value * 100);
            double formatted = rounded / 100.0;
            System.out.println("  - " + s.getId() + " (" + s.getType() + "): " + formatted);
        }
    }

    private static void viewAllActuatorStatus() {
        System.out.println("\nCurrent Actuator Status");
        Actuator[] allActuators = monitoringSystem.getActuators();
        for (int i = 0; i < allActuators.length; i++) {
            System.out.println("  - " + allActuators[i].getStatus());
        }
    }

    private static void viewThresholds() {
        System.out.println("\nSystem Automation Thresholds");
        String thresholdString = monitoringSystem.getThresholdsString();
        if (thresholdString.isEmpty()) {
            System.out.println("  No thresholds set.");
        } else {
            String[] pairs = thresholdString.split(", ");
            for (String pair : pairs) {
                System.out.println("  - " + pair);
            }
        }
    }

    private static void setThreshold() {
        try {
            System.out.println("Current Thresholds:");
            viewThresholds();
            System.out.print("Enter threshold key to change (e.g. Moisture_Low): ");
            String key = scanner.nextLine();
            
            System.out.print("Enter new value: ");
            // Double wrapper to parse 
            double value = Double.parseDouble(scanner.nextLine());
            
            monitoringSystem.setThreshold(key, value);
            System.out.println("Threshold updated successfully.");
        
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid number format.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    //shutdown - close file and scanner properly
    private static void shutdownSystem() {
        System.out.println("Shutting down Smart Agriculture System");
        logger.close();
        scanner.close();
        System.out.println("Goodbye.");
    }
}