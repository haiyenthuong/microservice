package com.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class cho Payment Service.
 * Lớp chính của ứng dụng Payment Service - điểm khởi đầu của Spring Boot.
 */
@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
@EntityScan("com.payment.*")
@EnableJpaRepositories("com.payment")
public class PaymentServiceApplication {

    /**
     * Main method để khởi chạy ứng dụng.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("========> Starting Payment Service...");
        SpringApplication.run(PaymentServiceApplication.class, args);
        System.out.println("========> Payment Service started successfully!");
    }
}
