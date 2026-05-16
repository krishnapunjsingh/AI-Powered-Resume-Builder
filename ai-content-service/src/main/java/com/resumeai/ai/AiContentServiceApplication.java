package com.resumeai.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiContentServiceApplication.class, args);
    }
}
