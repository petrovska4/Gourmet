package com.example.booking;

public class Restaurant {
    private String restaurantId;
    private String userId;
    private String name;
    private String address;
    private String phoneNumber;
    private double latitude;
    private double longitude;
    private double revSum;
    private double revCnt;

    public Restaurant() {
    }
    public Restaurant(String userId, String name, String address, String phoneNumber, double latitude, double longitude, double revSum, double revCnt) {
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.revSum = revSum;
        this.revCnt = revCnt;
    }

    public Restaurant(String restaurantId, String userId, String name, String address, String phoneNumber, double latitude, double longitude, double revSum, double revCnt) {
        this.restaurantId = restaurantId;
        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
        this.revSum = revSum;
        this.revCnt = revCnt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRevSum() {
        return revSum;
    }

    public void setRevSum(double revSum) {
        this.revSum = revSum;
    }

    public double getRevCnt() {
        return revCnt;
    }

    public void setRevCnt(double revCnt) {
        this.revCnt = revCnt;
    }
}
