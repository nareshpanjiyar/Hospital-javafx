package com.hospital.model;

public class User {
    private int id;
    private String username;
    private String name;
    private String role;
    private String email;

    public User() {}

    public User(int id, String username, String name) {
        this.id = id;
        this.username = username;
        this.name = name;
    }

    public User(int id, String username, String name, String role, String email) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
