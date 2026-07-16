# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

COPY pom.xml ./
COPY application/pom.xml application/pom.xml
COPY adapter-inbound-rest/pom.xml adapter-inbound-rest/pom.xml
COPY adapter-outbound-mongodb/pom.xml adapter-outbound-mongodb/pom.xml
COPY bootstrap/pom.xml bootstrap/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --quiet dependency:go-offline

COPY application/src application/src
COPY adapter-inbound-rest/src adapter-inbound-rest/src
COPY adapter-outbound-mongodb/src adapter-outbound-mongodb/src
COPY bootstrap/src bootstrap/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --quiet package -DskipTests

FROM eclipse-temurin:25-jre

RUN useradd --system --create-home appuser
WORKDIR /app

RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

COPY --from=builder --chown=appuser:appuser \
    /build/bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=65.0 -XX:MaxDirectMemorySize=64m -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD curl --fail --silent --show-error http://localhost:8080/actuator/health || exit 1

USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
