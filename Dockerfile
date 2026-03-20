FROM gradle:8.10.2-jdk21-alpine AS builder

ARG GRADLE_PROJECT
ARG SERVICE_PATH

WORKDIR /workspace

COPY build.gradle settings.gradle ./
COPY shared ./shared
COPY services ./services

RUN gradle --no-daemon "${GRADLE_PROJECT}:bootJar" && \
    cp "${SERVICE_PATH}/build/libs/app.jar" /tmp/app.jar

FROM eclipse-temurin:21-jre-alpine

ARG SERVICE_PORT=8080

WORKDIR /app

COPY --from=builder /tmp/app.jar /app/app.jar

EXPOSE ${SERVICE_PORT}

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
