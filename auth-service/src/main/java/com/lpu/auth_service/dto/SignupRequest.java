package com.lpu.auth_service.dto;
// This class represents the data transfer object for a signup request. 
// It contains fields for the user's name, email, password, role, and an optional admin key. 
// This DTO is used to capture the data sent by the client when a user attempts to sign up.
public class SignupRequest {

    private String name;
    private String email;
    private String password;
    private String role;
    private String adminKey;

    public SignupRequest() {}

    public SignupRequest(String name, String email, String password, String role, String adminKey) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.adminKey = adminKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(String adminKey) {
        this.adminKey = adminKey;
    }
}
