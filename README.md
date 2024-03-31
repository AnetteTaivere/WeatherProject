# Food Delivery Application

## Stack

* Java
* SpringBoot
* H2 database

## Description

This project pulls weather data and saves it to Database.
This application calculates the delivery fee for food couriers
based on regional base fee, vehicle type, and weather conditions.
It provides a REST interface for requesting delivery fees.

## Database

The application uses an H2 database to store weather data.
It includes a table with the following columns:

- Station Name
- WMO Code
- Air Temperature
- Wind Speed
- Weather Phenomenon
- Timestamp of Observations

## CronJob (Quartz)

The default frequency is once every hour, 15 minutes after a full hour (HH:15:00).
It can be changed in application.properties file, for example
cron.expression= 0 */2 * ? * *    for every even minute

## Delivery Fee Calculation

The delivery fee is calculated based on input parameters (city and vehicle type) and
weather data from the database.

## REST Interface

The REST interface provides an endpoint for requesting delivery fees.
It accepts parameters for city (Tallinn, Tartu, Pärnu) and vehicle type
(Car, Scooter, Bike). The response includes the total delivery fee or
an error message if applicable.

## Setup

To set up the application:

1. Clone the repository.
2. Setup H2 database locally
    - Download H2 Database
    - Start H2 Database Server (http://localhost:8082 should open)
    - Configure like this:
        - Saved Settings: Generic H2
        - JDBC URL: jdbc:h2:tcp://localhost/~/test
        - Username: sa
        - Password: sa
3. Run this code in database console to create table
   `CREATE TABLE WEATHER_DATA (
   ID INT PRIMARY KEY,
   NAME VARCHAR(255),
   WMO_CODE VARCHAR(255),
   AIR_TEMPERATURE DOUBLE,
   WIND_SPEED VARCHAR(255),
   WEATHER_PHENOMENON VARCHAR(255),
   DATE VARCHAR(255)
   );`
4. Build and run the Main class.

## Usage

To use the application:

1. Make a POST request to the endpoint `/fee` in terminal.
2. Include parameters `city` and `vehicle` in the request body.
3. Receive the calculated delivery fee or an error message in the response.

Example request body:
For Linux:
`curl -X POST -F 'city=Tartu' -F 'vehicle=bike' http://localhost:8080/fee`
For Windows:
`Invoke-RestMethod -Method Post -Uri "http://localhost:8080/fee" -Body @{city="Tallinn"; vehicle="car"}`

## Unit Test coverage (names: dataPullingTest, weatherProjectTests)

Metrics:

- Class Coverage: 100%
- Method Coverage: 77%
- Line Coverage: 80%