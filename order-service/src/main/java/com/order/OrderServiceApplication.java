package com.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class cho Order Service.
 * Lớp chính của ứng dụng Order Service - điểm khởi đầu của Spring Boot.
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
@EntityScan("com.order.*")
@EnableJpaRepositories("com.order")
public class OrderServiceApplication {

    /**
     * Main method để khởi chạy ứng dụng.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("========> Starting Order Service...");
        SpringApplication.run(OrderServiceApplication.class, args);
        System.out.println("========> Order Service started successfully!");
    }
}
