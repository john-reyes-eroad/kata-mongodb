# syntax=docker/dockerfile:1
# Java runtime flavor
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /build

RUN apt-get update \
  && apt-get install --no-install-recommends --yes maven \
  && rm -rf /var/lib/apt/lists/*

COPY pom.xml ./
COPY application/pom.xml application/pom.xml
COPY adapter-inbound-rest/pom.xml adapter-inbound-rest/pom.xml
COPY adapter-outbound-mongodb/pom.xml adapter-outbound-mongodb/pom.xml
COPY bootstrap/pom.xml bootstrap/pom.xml
COPY manual-runner/pom.xml manual-runner/pom.xml
COPY lambda-authorizer/pom.xml lambda-authorizer/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --quiet -pl bootstrap -am dependency:go-offline

COPY application/src application/src
COPY adapter-inbound-rest/src adapter-inbound-rest/src
COPY adapter-outbound-mongodb/src adapter-outbound-mongodb/src
COPY bootstrap/src bootstrap/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --quiet -pl bootstrap -am package -DskipTests

FROM debian:bookworm-slim AS runtime-assets

RUN apt-get update \
  && apt-get install --no-install-recommends --yes busybox-static \
  && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=runtime-assets /bin/busybox /busybox
COPY --from=builder /build/bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar /app/app.jar

RUN useradd --system --uid 10001 --create-home appuser

ENV SERVER_PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:+UseZGC"
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD ["/busybox", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]

USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
