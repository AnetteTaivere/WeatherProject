package com.example.demo2;

public class JsonRequest {

    private String city;
    private String vehicle;

    public JsonRequest(String city, String vehicle) {
        this.city = city;
        this.vehicle = vehicle;
    }

    // Getters and setters
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

}
