package com.smartagri.users;

// Defines user roles for the system
// Uses string constants instead of enums for hand-written feel
public class Role {
    // Role constants - different user types
    public static final String ADMIN = "ADMIN";           // Can configure system
    public static final String AGRONOMIST = "AGRONOMIST"; // Can analyze data
    public static final String FARMER = "FARMER";         // Can view data
    
    private String roleName;
    
    // Create a new Role instance
    public Role(String roleName) {
        this.roleName = roleName;
    }
    
    // Get the role name
    public String getRoleName() {
        return roleName;
    }
    
    // Check if a role name is valid
    public static boolean isValidRole(String roleName) {
        return roleName.equals(ADMIN) || 
               roleName.equals(AGRONOMIST) || 
               roleName.equals(FARMER);
    }
}