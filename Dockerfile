# Multi-stage build for Spring Boot application
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime with minimal Java image
FROM eclipse-temurin:25-jre

WORKDIR /app

# Install runtime tooling used by health checks
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from builder stage
COPY --from=builder /build/target/kata-mongodb-0.0.1-SNAPSHOT.jar app.jar

# Set environment variables
ENV SERVER_PORT=8080

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT exec java -jar app.jar
