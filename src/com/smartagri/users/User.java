package com.smartagri.users;

// Stores login info and role
public class User {
    private String username;           // Login name
    private String hashedPassword;     // Simulated hashed password
    private String role;               // User's role string

    // create user with all details
    public User(String username, String password, String role) {
        this.username = username;
        // Simulate hashing the password
        this.hashedPassword = hashPassword(password);
        this.role = role;
    }

    //default role is FARMER
    public User(String username, String password) {
        this(username, password, Role.FARMER);
    }

    // password hashing
    // Reverse the string and add "_hashed" suffix
    private String hashPassword(String password) {
        String reversed = "";
        for (int i = password.length() - 1; i >= 0; i--) {
            reversed = reversed + password.charAt(i);
        }
        return reversed + "_hashed";
    }

    // Check if provided password is correct
    public boolean checkPassword(String password) {
        return this.hashedPassword.equals(hashPassword(password));
    }

    // Get the username
    public String getUsername() {
        return username;
    }

    // Get the user's role
    public String getRole() {
        return role;
    }
}