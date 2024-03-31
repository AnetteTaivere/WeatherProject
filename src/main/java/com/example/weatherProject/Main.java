package com.example.weatherProject;

import org.quartz.SchedulerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
@RequestMapping("")
public class Main {

    /**
     * Calculates the delivery fee based on the provided city and vehicle type.
     *
     * @param city    City comes from curl first parameter
     * @param vehicle Vehicle comes from curl second parameter
     * @return A string indicating the total fee for the delivery, or an informative message if there's an issue.
     * @PostMapping("/fee") handles POST requests targeted at the "/fee"
     */
    @PostMapping("/fee")
    @ResponseBody
    public static String calculateDeliveryFee(@RequestParam("city") String city, @RequestParam("vehicle") String vehicle) {
        double total = DeliveryFeeCalculator.calculateFee(city, vehicle);
        if (total > 0) {
            return "Total fee: " + total;
        } else {
            if (total == -1.0) return "Wrong city parameter! Try Tallinn, Tartu or Pärnu.";
            if (total == -2) return "Wrong vehicle parameter! Try Bike, Scooter or Car.";
            if (total == -10) return "No weather data found for city: " + city;
            if (total == -20) return "Usage of selected vehicle type is forbidden due to high wind speed.";
            if (total == -30) return "Usage of selected vehicle type is forbidden.";
            else
                return "Something went wrong! ";
        }
    }

    /**
     * Entry point for the application. Starts the Spring application
     * context and schedules the CronJob.
     *
     * @param args Command line arguments (not used).
     * @throws SchedulerException If there is an error scheduling the CronJob.
     */
    public static void main(String[] args) throws SchedulerException {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        CronJob cron = context.getBean(CronJob.class);
        cron.executeTask();
    }
}