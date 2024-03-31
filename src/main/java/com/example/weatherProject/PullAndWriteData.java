package com.example.weatherProject;

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

public class PullAndWriteData implements Job {

    /**
     * Executes the scheduled job to fetch weather data from a remote URL,
     * parse the XML data, and insert it into the database.
     *
     * @param context           Context from Main method
     * @throws RuntimeException If there is an error executing the job
     */
    public void execute(JobExecutionContext context) throws RuntimeException {
        try {
            // Fetch XML data from the remote URL
            String xmlData = fetchXMLData();

            // Parse and process the XML data
            processXMLData(xmlData);
            System.out.println("Data inserted successfully!");
        } catch (ParserConfigurationException | SQLException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    // Method to fetch XML data from the remote URL
    private String fetchXMLData() throws IOException {
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
            return response.toString();
        } else {
            throw new IOException("Failed to fetch XML. Response code: " + responseCode);
        }
    }


    // Method to parse and process the XML data
    private void processXMLData(String xmlData) throws ParserConfigurationException, SQLException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        InputSource inputSource = new InputSource(new StringReader(xmlData));
        Document doc = dBuilder.parse(inputSource);
        doc.getDocumentElement().normalize();

        NodeList stationList = doc.getElementsByTagName("station");

        String date = extractTimestamp(doc);

        try (Connection dbConn = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/test", "sa", "sa")) {
            String sql = "INSERT INTO WEATHER_DATA (name, wmo_code, air_temperature, wind_speed, weather_phenomenon, date) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = dbConn.prepareStatement(sql);

            for (int i = 0; i < stationList.getLength(); i++) {
                Element stationElement = (Element) stationList.item(i);
                String name = stationElement.getElementsByTagName("name").item(0).getTextContent();
                if (isSupportedCity(name)) {
                    extractAndInsertData(stationElement, preparedStatement, date, name);
                }
            }
        }
    }

    // Method to extract the timestamp from the XML data
    private String extractTimestamp(Document doc) {
        String[] timeStampSplit = doc.getElementsByTagName("observations").item(0).getAttributes().getNamedItem("timestamp").getTextContent().split("\"");
        long unixTime = Long.parseLong(timeStampSplit[1]);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        return dateTime.format(formatter);
    }

    // Method to extract and insert data into the database
    private void extractAndInsertData(Element stationElement, PreparedStatement preparedStatement, String date, String name) throws SQLException {
        String[] xmlSplit = stationElement.getTextContent().split("\t\t");
        String wmocode = xmlSplit[2];
        String weatherPhenomenon = xmlSplit[5];
        double airTemperature = xmlSplit[10].trim().isEmpty() ? 100.0 : Double.parseDouble(xmlSplit[10]);
        String windSpeed = xmlSplit[12];

        preparedStatement.setString(1, name);
        preparedStatement.setString(2, wmocode);
        preparedStatement.setDouble(3, airTemperature);
        preparedStatement.setString(4, windSpeed);
        preparedStatement.setString(5, weatherPhenomenon);
        preparedStatement.setString(6, date);
        preparedStatement.executeUpdate();
    }

    // Method to check if a city is supported
    private boolean isSupportedCity(String name) {
        return name.equals("Tallinn-Harku") || name.equals("Tartu-Tõravere") || name.equals("Pärnu");
    }
}