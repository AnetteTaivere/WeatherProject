package com.example.demo2;

class WeatherNotFoundException  extends RuntimeException {

    WeatherNotFoundException(Long id) {
        super("Could not find weather " + id);
    }
}
