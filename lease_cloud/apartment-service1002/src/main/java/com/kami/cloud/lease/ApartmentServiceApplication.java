package com.kami.cloud.lease;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
@MapperScan(basePackages = {
        "com.kami.cloud.lease.web.admin.mapper",
        "com.kami.cloud.lease.web.app.mapper",
        "com.kami.cloud.lease.web.inner.mapper"
}, nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
@SpringBootApplication(scanBasePackages = "com.kami.cloud.lease",
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class ApartmentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApartmentServiceApplication.class, args);
    }
}