package com.kami.cloud.lease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.kami.cloud.lease",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}