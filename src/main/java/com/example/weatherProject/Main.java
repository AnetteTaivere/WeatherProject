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


    @PostMapping("/fee")
    @ResponseBody
    public String calculateDeliveryFee(@RequestParam("city") String city, @RequestParam("vehicle") String vehicle) {

        double total = DeliveryFeeCalculator.calculateDeliveryFee(city, vehicle);
        if (total > 0) {
            return "Total fee: " + total;
        } else {
            if (total == -1.0) {
                return "Wrong city parameter! Try Tallinn, Tartu or Pärnu.";
            }
            if (total == -2) {
                return "Wrong vehicle parameter! Try Bike, Scooter or Car.";
            }
            if (total == -10) {
                return "No weather data found for city: " + city;
            }
            if (total == -20) {
                return "Usage of selected vehicle type is forbidden due to high wind speed.";
            }
            else return "Usage of selected vehicle type is forbidden.";
        }
    }

    public static void main(String[] args) throws SchedulerException {
        ApplicationContext context = SpringApplication.run(Main.class, args);
        CronJob cron = context.getBean(CronJob.class);
        cron.executeTask();
    }
}