package com.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.cms.infrastructure.config.ConfigProperties;

/** Điểm khởi động ứng dụng Spring Boot. */

@SpringBootApplication
@EnableScheduling
@EntityScan("com.cms.*")
@EnableJpaRepositories("com.cms")
public class CmsServiceApplication {

    /**
     * Hàm main khởi động ứng dụng. Thiết lập logging và chạy Spring Boot.
     *
     * @param args tham số dòng lệnh
     */
    public static void main(String[] args) {
        System.out.println("========> application-"
                + ConfigProperties.getConfigProperties("spring.profiles.active", "application.yml")
                + ".properties");
        SpringApplication.run(CmsServiceApplication.class, args);
    }
}
