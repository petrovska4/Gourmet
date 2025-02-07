package com.example.booking;

public class User {
    private String userId;
    private String username;
    private String email;
    private String phone;
    private boolean isRestaurateur;
    public User(String username, String email, String phone, boolean isRestaurateur) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.isRestaurateur = isRestaurateur;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isRestaurateur() {
        return isRestaurateur;
    }

    public void setRestaurateur(boolean restaurateur) {
        isRestaurateur = restaurateur;
    }
}
