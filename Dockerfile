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

FROM eclipse-temurin:25-jdk-alpine AS runtime-builder

RUN jlink \
    --add-modules java.se,java.instrument,java.management,java.naming,java.security.jgss,jdk.crypto.ec,jdk.unsupported \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=zip-6 \
    --output /opt/java-minimal

FROM alpine:3.23

RUN apk add --no-cache ca-certificates libstdc++ \
  && addgroup -S appuser \
  && adduser -S -G appuser appuser

WORKDIR /app

COPY --from=runtime-builder /opt/java-minimal /opt/java/openjdk
COPY --from=builder --chown=appuser:appuser \
    /build/bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar app.jar

ENV JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:${PATH}" \
    SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=65.0 -XX:MaxDirectMemorySize=64m -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD wget --quiet --spider http://localhost:8080/actuator/health || exit 1

USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]
