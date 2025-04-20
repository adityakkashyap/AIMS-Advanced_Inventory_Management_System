package com.inventory.model;

public class User {
    private final int id;
    private final String username;
    private final String password;
    private final UserRole role;

    public User(int id, String username, String password, UserRole role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
}