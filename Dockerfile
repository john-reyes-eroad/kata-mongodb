# syntax=docker/dockerfile:1
FROM ghcr.io/graalvm/native-image-community:25 AS builder

WORKDIR /build

RUN microdnf install --assumeyes maven \
  && microdnf clean all

COPY pom.xml ./
COPY application/pom.xml application/pom.xml
COPY adapter-inbound-rest/pom.xml adapter-inbound-rest/pom.xml
COPY adapter-outbound-mongodb/pom.xml adapter-outbound-mongodb/pom.xml
COPY bootstrap/pom.xml bootstrap/pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --quiet -pl bootstrap -am -Pnative dependency:go-offline

COPY application/src application/src
COPY adapter-inbound-rest/src adapter-inbound-rest/src
COPY adapter-outbound-mongodb/src adapter-outbound-mongodb/src
COPY bootstrap/src bootstrap/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn --batch-mode --quiet -pl bootstrap -am -Pnative native:compile

FROM debian:bookworm-slim AS runtime-assets

RUN apt-get update \
  && apt-get install --no-install-recommends --yes busybox-static \
  && find /usr/lib -name 'libz.so.1.*' -type f -exec cp {} /opt/libz.so.1 \; \
  && rm -rf /var/lib/apt/lists/*

FROM gcr.io/distroless/cc-debian12:nonroot

WORKDIR /app

COPY --from=runtime-assets /bin/busybox /busybox
COPY --from=runtime-assets /opt/libz.so.1 /lib/libz.so.1
COPY --from=builder --chown=nonroot:nonroot \
    /build/bootstrap/target/kata-mongodb /app/kata-mongodb

ENV LD_LIBRARY_PATH=/lib \
    SERVER_PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD ["/busybox", "wget", "-q", "--spider", "http://localhost:8080/actuator/health"]

USER nonroot
ENTRYPOINT ["/app/kata-mongodb"]
