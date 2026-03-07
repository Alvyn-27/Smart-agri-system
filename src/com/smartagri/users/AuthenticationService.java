package com.smartagri.users;

// Handles user registration and login
// Keeps track of all users in a simple array
public class AuthenticationService {
    // Array to store registered users
    private User[] userDatabase;
    // Keep track of how many users are registered
    private int userCount;
    // Initial capacity of the array
    private static final int INITIAL_CAPACITY = 10;

    // Create the service and add some default test users
    public AuthenticationService() {
        this.userDatabase = new User[INITIAL_CAPACITY];
        this.userCount = 0;
        
        // Pre-populate with default users for testing
        register("admin", "admin123", Role.ADMIN);
        register("agro", "agro123", Role.AGRONOMIST);
        register("farmer", "farmer123", Role.FARMER);
    }

    // Try to register a new user
    // Returns true if successful, false if user already exists
    public boolean register(String username, String password, String role) {
        // Check if username is already taken
        for (int i = 0; i < userCount; i++) {
            if (userDatabase[i].getUsername().equals(username)) {
                return false;
            }
        }
        
        // Expand array if we're running out of space
        if (userCount >= userDatabase.length) {
            expandArray();
        }
        
        // Create and store new user
        User newUser = new User(username, password, role);
        userDatabase[userCount] = newUser;
        userCount++;
        return true;
    }

    // Try to log in a user
    // Throws AuthenticationException with details about what went wrong
    public User login(String username, String password) throws AuthenticationException {
        // Search for user in the array
        User user = null;
        for (int i = 0; i < userCount; i++) {
            if (userDatabase[i].getUsername().equals(username)) {
                user = userDatabase[i];
                break;
            }
        }

        // Make sure user exists
        if (user == null) {
            String errorMsg = "Login failed: Username '" + username + "' not found.";
            throw new AuthenticationException(errorMsg, true, false);
        }

        // Verify password
        if (!user.checkPassword(password)) {
            String errorMsg = "Login failed: Incorrect password for user '" + username + "'.";
            throw new AuthenticationException(errorMsg, false, true);
        }

        // Success - return the user
        return user;
    }

    // Expand the array when we need more space
    private void expandArray() {
        User[] newArray = new User[userDatabase.length * 2];
        for (int i = 0; i < userCount; i++) {
            newArray[i] = userDatabase[i];
        }
        userDatabase = newArray;
    }
}