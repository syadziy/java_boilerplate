package com.mac.boilerplate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class JavaBoilerplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaBoilerplateApplication.class, args);
    }
}
