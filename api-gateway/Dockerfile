# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml và download dependencies (tận dụng Docker layer cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy JAR file từ build stage
COPY --from=build /app/target/api-gateway-*.jar app.jar

# Create directory cho logs
RUN mkdir -p /app/logs

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}", \
    "-jar", \
    "app.jar"]

# Optional: Add JVM options for production
# ENTRYPOINT ["java", \
#     "-XX:+UseContainerSupport", \
#     "-XX:MaxRAMPercentage=75.0", \
#     "-Djava.security.egd=file:/dev/./urandom", \
#     "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}", \
#     "-jar", \
#     "app.jar"]
