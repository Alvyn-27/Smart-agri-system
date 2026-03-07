package com.smartagri.core;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

// handles writing logs to a file
public class FileLogger {
    
    private static final String LOG_FILE = "smart_agri_log.txt";
    private PrintWriter writer;
    
    // open file in append mode
    public FileLogger() {
        try {
            writer = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not open log file. " + e.getMessage());
        }
    }

    // log message with timestamp
    public void log(String message) {
        // get current time
        Date date = new Date();
        // format time as hh:mm:ss
        String timestamp = String.format("%tT", date); 
        
        String logEntry = String.format("[%s] %s", timestamp, message);
        
        // write to file
        if (writer != null) {
            writer.println(logEntry);
        }
    }

    // overload to add category prefix
    public void log(String category, String message) {
        log(category + ": " + message);
    }

    // log a simple event
    public void logEvent(String event) {
        log("EVENT: " + event);
    }

    // handle variable arguments for details
    public void logEvent(String event, String... details) {
        // join details with comma
        String detailString = String.join(", ", details);
        log("EVENT", event + " (" + detailString + ")");
    }

    // close file writer safely
    public void close() {
        if (writer != null) {
            log("Logger shutting down.");
            writer.close();
        }
    }
}