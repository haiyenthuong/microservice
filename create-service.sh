#!/bin/bash

# Script tạo mới một microservice
# Usage: ./create-service.sh <service-name>

if [ -z "$1" ]; then
    echo "❌ Usage: ./create-service.sh <service-name>"
    echo "   Example: ./create-service.sh user-service"
    exit 1
fi

SERVICE_NAME=$1
SERVICE_DIR="$(pwd)/$SERVICE_NAME"

echo "🚀 Creating new microservice: $SERVICE_NAME"
echo "📁 Location: $SERVICE_DIR"

# Tạo cấu trúc thư mục
mkdir -p "$SERVICE_DIR/src/main/java/com/$SERVICE_NAME"/{domain/{application,infrastructure,interfaces}/{application/{dto,service,command,query},domain/{model,repository,service},infrastructure/{persistence,config,mapper},interfaces/{rest,dto}}}
mkdir -p "$SERVICE_DIR/src/main/resources"
mkdir -p "$SERVICE_DIR/src/test/java/com/$SERVICE_NAME"

# Tạo pom.xml cho service mới
cat > "$SERVICE_DIR/pom.xml" << EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.microservices</groupId>
        <artifactId>microservice-parent</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>$SERVICE_NAME</artifactId>
    <name>$SERVICE_NAME</name>
    <description>$SERVICE_NAME - Auto-generated</description>
    <packaging>jar</packaging>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Data JPA -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>

        <!-- MapStruct -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>

        <!-- Swagger/OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.security</groupId>
            <artifactId>spring-security-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>
EOF

# Tạo application.yml
cat > "$SERVICE_DIR/src/main/resources/application.yml" << EOF
spring:
  application:
    name: $SERVICE_NAME
  profiles:
    active: dev
  jpa:
    open-in-view: false
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

server:
  port: 8082

# JWT Configuration
jwt:
  secret: 5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
  expiration: 86400000

springdoc:
  api-docs:
    enabled: true
    path: /api-docs
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
EOF

# Tạo application-dev.yml
cat > "$SERVICE_DIR/src/main/resources/application-dev.yml" << EOF
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/${SERVICE_NAME}db?createDatabaseIfNotExist=true
    username: root
    password: 123456aA@
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

logging:
  level:
    com.$SERVICE_NAME: DEBUG
EOF

# Tạo Main Application class
cat > "$SERVICE_DIR/src/main/java/com/$SERVICE_NAME/${SERVICE_NAME^}Application.java" << EOF
package com.$SERVICE_NAME;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ${SERVICE_NAME^}Application {

    public static void main(String[] args) {
        SpringApplication.run(${SERVICE_NAME^}Application.class, args);
    }
}
EOF

# Tạo .gitignore
cat > "$SERVICE_DIR/.gitignore" << EOF
target/
*.class
*.log
.DS_Store
EOF

# Thêm module vào parent POM
echo ""
echo "📝 Adding module to parent POM..."
sed -i "/<modules>/a\        <module>$SERVICE_NAME</module>" pom.xml

echo ""
echo "✅ Service '$SERVICE_NAME' created successfully!"
echo ""
echo "📋 Next steps:"
echo "   1. Review the generated files in: $SERVICE_DIR"
echo "   2. Update database configuration in src/main/resources/application-dev.yml"
echo "   3. Build all services: mvn clean install"
echo "   4. Run the service: cd $SERVICE_NAME && mvn spring-boot:run"
echo ""
echo "🔗 Default port: 8082 (change in application.yml if needed)"
