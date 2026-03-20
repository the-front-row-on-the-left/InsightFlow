FROM gradle:8.10.2-jdk21 AS builder

WORKDIR /workspace

COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY shared shared
COPY services services

RUN gradle :services:gateway-service:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /workspace/services/gateway-service/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
