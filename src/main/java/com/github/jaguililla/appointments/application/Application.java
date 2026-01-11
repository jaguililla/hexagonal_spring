package com.github.jaguililla.appointments.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.github.jaguililla.appointments")
class Application {

    static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }
}
