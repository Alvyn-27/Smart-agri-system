package com.smartagri.irrigation_shade;

// Interface for objects that can provide logging data
// Anything that needs to be logged implements this
public interface Loggable {
    String getLogData();
}