package com.example.demo;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
public record AwsConfiguration(

        String bucket
) {

}
