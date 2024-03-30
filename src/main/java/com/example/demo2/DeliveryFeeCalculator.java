package com.example.demo2;

import java.io.PrintStream;
import java.sql.*;

public class DeliveryFeeCalculator {

    public static double calculateDeliveryFee(String city, String vehicle) {
        // Convert city and vehicle to lowercase
        city = city.toLowerCase();
        vehicle = vehicle.toLowerCase();
        // Validate city and vehicle inputs
        if (!(city.equals("tallinn") || city.equals("tartu") || city.equals("pärnu"))) {
            return -1.0;
        }
        if (!(vehicle.equals("bike") || vehicle.equals("car") || vehicle.equals("scooter"))) {
            return -2.0;
        }
        // Calculate base and weather fees
        double base = calculateReginalBaseFee(city, vehicle);
        double weatherFee = calculateWeather(city, vehicle);
        if (weatherFee <= -10) return weatherFee;

        return base + weatherFee;
    }

    private static double calculateReginalBaseFee(String city, String vehicle) {
        double baseFee = 0.0;
        // Determine the base fee based on city and vehicle type
        switch (city) {
            case "tallinn":
                switch (vehicle) {
                    case "car" -> baseFee = 4.0;
                    case "scooter" -> baseFee = 3.5;
                    case "bike" -> baseFee = 3.0;
                }
                break;
            case "tartu":
                switch (vehicle) {
                    case "car" -> baseFee = 3.5;
                    case "scooter" -> baseFee = 3.0;
                    case "bike" -> baseFee = 2.5;
                }
                break;
            case "pärnu":
                switch (vehicle) {
                    case "car" -> baseFee = 3.0;
                    case "scooter" -> baseFee = 2.5;
                    case "bike" -> baseFee = 2.0;
                }
                break;
        }
        return baseFee;
    }

    private static double calculateWeather(String city, String vehicle) {

        double extraFees = 0.0;
        try (Connection connection = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test", "sa", "sa")) {
            PreparedStatement preparedStatement = null;
            // Prepare SQL statement based on city
            switch (city) {
                case "tallinn" ->
                        preparedStatement = connection.prepareStatement("SELECT * FROM WEATHER_DATA WHERE WEATHER_DATA.NAME = 'Tallinn-Harku'  ORDER BY DATE DESC LIMIT 1");
                case "tartu" ->
                        preparedStatement = connection.prepareStatement("SELECT * FROM WEATHER_DATA WHERE WEATHER_DATA.NAME = 'Tartu-Tõravere'  ORDER BY DATE DESC LIMIT 1");
                case "pärnu" ->
                        preparedStatement = connection.prepareStatement("SELECT * FROM WEATHER_DATA WHERE WEATHER_DATA.NAME = 'Pärnu'  ORDER BY DATE DESC LIMIT 1");
            }
            // If preparedStatement is still null, return a default error value
            if (preparedStatement == null) {
                return -100;
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If no weather data found, return 0
                if (!resultSet.next()) {
                    return -10;
                    //throw new CalculationException("No weather data found for city: " + city);
                }
                double airTemperature = resultSet.getDouble("air_temperature");
                double windSpeed = resultSet.getDouble("wind_speed");
                String weatherPhenomenon = resultSet.getString("weather_phenomenon").toLowerCase();

                // Calculate extra fees based on air temperature
                double airTemperatureExtraFee = 0.0;
                if ((vehicle.equals("scooter") || vehicle.equals("bike")) && airTemperature < -10) {
                    airTemperatureExtraFee = 1.0;
                } else if ((vehicle.equals("scooter") || vehicle.equals("bike")) && airTemperature >= -10 && airTemperature < 0) {
                    airTemperatureExtraFee = 0.5;
                }

                // Calculate extra fees based on wind speed
                double windSpeedExtraFee = 0.0;
                if (vehicle.equals("bike")) {
                    if (windSpeed >= 10 && windSpeed <= 20) {
                        windSpeedExtraFee = 0.5;
                    } else if (windSpeed > 20) {
                        return -20;
                        //throw new CalculationException("Usage of selected vehicle type is forbidden due to high wind speed");

                    }
                }

                // Calculate extra fees based on weather phenomenon
                double weatherPhenomenonExtraFee = 0.0;
                if ((vehicle.equals("scooter") || vehicle.equals("bike"))) {
                    if (weatherPhenomenon.contains("snow") || weatherPhenomenon.contains("sleet")) {
                        weatherPhenomenonExtraFee = 1.0;
                    } else if (weatherPhenomenon.contains("rain")) {
                        weatherPhenomenonExtraFee = 0.5;
                    } else if (weatherPhenomenon.contains("glaze") || weatherPhenomenon.contains("hail") || weatherPhenomenon.contains("thunder")) {
                        return -30;
                        //throw new CalculationException("Error: Usage of selected vehicle type is forbidden");
                    }
                }

                // Calculate total extra fees
                return airTemperatureExtraFee + windSpeedExtraFee + weatherPhenomenonExtraFee;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}