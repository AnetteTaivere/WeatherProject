package com.example.weatherProject;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class weatherProjectTests {


    // Test invalid city input
    @Test
    public void testCalculateDeliveryFee_InvalidCity() {
        assertEquals("Wrong city parameter! Try Tallinn, Tartu or Pärnu.", Main.calculateDeliveryFee("Riga", "Car"));
    }

    // Test invalid vehicle input
    @Test
    public void testCalculateDeliveryFee_InvalidVehicle() throws SQLException {
        assertEquals("Wrong vehicle parameter! Try Bike, Scooter or Car.", Main.calculateDeliveryFee("Tartu", "Bikeee"));
    }

    // Test case where there is high wind speed
    @Test
    public void testCalculateDeliveryFee_HighWindSpeed() {
        String url = "jdbc:h2:tcp://localhost/~/test";
        String user = "sa";
        String password = "sa";

        // SQL statement for the insert
        String sql = "INSERT INTO WEATHER_DATA (ID, name, wmo_code, air_temperature, wind_speed, weather_phenomenon, date) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "Tartu-Tõravere");
            preparedStatement.setString(2, "26242");
            preparedStatement.setDouble(3, 10.0);
            preparedStatement.setString(4, String.valueOf(25.0));
            preparedStatement.setString(5, "");
            LocalDateTime now = LocalDateTime.now();

            // Format the date and time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);
            preparedStatement.setString(6, formattedDateTime);
            preparedStatement.executeUpdate();

            assertEquals("Usage of selected vehicle type is forbidden due to high wind speed.", Main.calculateDeliveryFee("Tartu", "Bike"));

            String sqlDelete = "DELETE FROM WEATHER_DATA WHERE ID = (SELECT MAX(ID) FROM WEATHER_DATA);";
            PreparedStatement preparedStatementDelete = connection.prepareStatement(sqlDelete);
            preparedStatementDelete.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test case with valid inputs
    @Test
    public void testCalculateDeliveryFee_ValidInputs() {
        String url = "jdbc:h2:tcp://localhost/~/test";
        String user = "sa";
        String password = "sa";

        // SQL statement for the insert
        String sql = "INSERT INTO WEATHER_DATA (ID, name, wmo_code, air_temperature, wind_speed, weather_phenomenon, date) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "Tallinn-Harku");
            preparedStatement.setString(2, "26038");
            preparedStatement.setDouble(3, 10.0);
            preparedStatement.setString(4, String.valueOf(10.0));
            preparedStatement.setString(5, "");
            LocalDateTime now = LocalDateTime.now();

            // Format the date and time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);
            preparedStatement.setString(6, formattedDateTime);
            preparedStatement.executeUpdate();
            assertEquals("Total fee: " + 4.0, Main.calculateDeliveryFee("Tallinn", "Car"));

            String sqlDelete = "DELETE FROM WEATHER_DATA WHERE ID = (SELECT MAX(ID) FROM WEATHER_DATA);";
            PreparedStatement preparedStatementDelete = connection.prepareStatement(sqlDelete);
            preparedStatementDelete.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Test case where weather phenomenon is forbidden
    @Test
    public void testCalculateDeliveryFee_ForbiddenWeatherPhenomenon() {
        String url = "jdbc:h2:tcp://localhost/~/test";
        String user = "sa";
        String password = "sa";

        // SQL statement for the insert
        String sql = "INSERT INTO WEATHER_DATA (ID, name, wmo_code, air_temperature, wind_speed, weather_phenomenon, date) VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "Pärnu");
            preparedStatement.setString(2, "41803");
            preparedStatement.setDouble(3, -10.0);
            preparedStatement.setString(4, String.valueOf(15.0));
            preparedStatement.setString(5, "glaze");
            LocalDateTime now = LocalDateTime.now();

            // Format the date and time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = now.format(formatter);
            preparedStatement.setString(6, formattedDateTime);
            preparedStatement.executeUpdate();
            // Test invalid vehicle input
            assertEquals("Usage of selected vehicle type is forbidden.", Main.calculateDeliveryFee("Pärnu", "Scooter"));

            String sqlDelete = "DELETE FROM WEATHER_DATA WHERE ID = (SELECT MAX(ID) FROM WEATHER_DATA);";
            PreparedStatement preparedStatementDelete = connection.prepareStatement(sqlDelete);
            preparedStatementDelete.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}