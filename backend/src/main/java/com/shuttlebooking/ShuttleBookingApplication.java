package com.shuttlebooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShuttleBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShuttleBookingApplication.class, args);
    }
}
