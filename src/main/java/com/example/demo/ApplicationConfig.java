package com.example.demo;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableFeignClients
@EnableMongoRepositories
public class ApplicationConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
