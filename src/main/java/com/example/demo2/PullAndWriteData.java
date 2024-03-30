package com.example.demo2;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class PullAndWriteData implements Job {
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String dbUrl = "jdbc:h2:tcp://localhost/~/test";
        String sql = "INSERT INTO WEATHER_DATA (name, wmo_code, air_temperature, wind_speed, weather_phenomenon, date) VALUES (?, ?, ?, ?, ?, ?)";


        try (Connection dbConn = DriverManager.getConnection(dbUrl, "sa", "sa")) {
            URL url = new URL("https://www.ilmateenistus.ee/ilma_andmed/xml/observations.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                String xmlData = response.toString();
                String[] split = xmlData.split("<station>");

                ArrayList<String> dataList = new ArrayList<>(Arrays.asList(split));
                // Timestamp to String
                String[] timeStampSplit = dataList.get(0).split("<observations timestamp=");
                String[] time = Arrays.stream(timeStampSplit).toList().get(1).split("\"");
                long unixTime = Long.parseLong(time[1]);
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneOffset.UTC);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
                String date = dateTime.format(formatter);

                // Loop over each XML string
                try {
                    // Parse XML string
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    InputSource inputSource = new InputSource(new StringReader(xmlData));
                    Document doc = dBuilder.parse(inputSource);
                    doc.getDocumentElement().normalize();

                    NodeList stationList = doc.getElementsByTagName("station");


                    // Process each <station> element individually
                    String name = null;
                    String wmocode = null;
                    Double airTemperature = null;
                    String windSpeed = null;
                    String weatherPhenomenon = null;
                    for (int i = 0; i < stationList.getLength(); i++) {

                        Element stationElement = (Element) stationList.item(i);
                        name = stationElement.getElementsByTagName("name").item(0).getTextContent();
                        if (name.equals("Tallinn-Harku") || name.equals("Tartu-Tõravere") || name.equals("Pärnu")) {


                            String[] xmlSplit = stationElement.getTextContent().split("\t\t");
                            wmocode = xmlSplit[2];
                            weatherPhenomenon = xmlSplit[5];
                            if (!xmlSplit[10].trim().isEmpty()) {
                                airTemperature = Double.valueOf(xmlSplit[10]);
                            } else {
                                airTemperature = 100.0;
                            }
                            windSpeed = xmlSplit[12];


                            // write to Database
                            PreparedStatement preparedStatement = dbConn.prepareStatement(sql);
                            preparedStatement.setString(1, name);
                            preparedStatement.setString(2, wmocode);
                            preparedStatement.setDouble(3, airTemperature);
                            preparedStatement.setString(4, windSpeed);
                            preparedStatement.setString(5, weatherPhenomenon);
                            preparedStatement.setString(6, date);
                            //execute statement
                            preparedStatement.executeUpdate();
                        }
                    }

                    System.out.println("Data inserted successfully!");

                } catch (ParserConfigurationException | SAXException e) {
                    throw new RuntimeException(e);
                }
            } else {
                System.out.println("Failed to fetch XML. Response code: " + responseCode);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
