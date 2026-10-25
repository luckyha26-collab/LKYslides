# Multi-stage Docker build for Spring Boot 3.2 (Java 21)
# Stage 1: Build JAR using Maven with Eclipse Temurin 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy sources and package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal, secure JRE 21 runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

# Render injects the PORT environment variable
ENV PORT=8081
EXPOSE 8081

# JVM flags tailored for Render Free Tier (512MB RAM):
# Uses cgroup memory detection, caps heap at 75% (~380MB), prevents out-of-memory container kills
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
