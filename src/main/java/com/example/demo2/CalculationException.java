package com.example.demo2;

public class CalculationException extends RuntimeException {

    private final String userMessage;

    public CalculationException(String message) {
        super(message);
        this.userMessage = message;
    }

    public CalculationException(String message, Throwable cause) {
        super(message, cause);
        this.userMessage = message;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
