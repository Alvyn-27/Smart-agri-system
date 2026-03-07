package com.smartagri.users;

// Custom exception for handling login failures
// Can specify whether username, password, or both are wrong
public class AuthenticationException extends Exception {
    private boolean userNotFound;      // True if username doesn't exist
    private boolean passwordIncorrect; // True if password is wrong

    // Constructor for generic error message
    public AuthenticationException(String message) {
        super(message);
        this.userNotFound = false;
        this.passwordIncorrect = false;
    }

    // Constructor that specifies what went wrong
    public AuthenticationException(String message, boolean userNotFound, boolean passwordIncorrect) {
        super(message);
        this.userNotFound = userNotFound;
        this.passwordIncorrect = passwordIncorrect;
    }

    // Check if the error was user not found
    public boolean isUserNotFound() {
        return userNotFound;
    }

    // Check if the error was incorrect password
    public boolean isPasswordIncorrect() {
        return passwordIncorrect;
    }
}