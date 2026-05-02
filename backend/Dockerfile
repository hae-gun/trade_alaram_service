# Kotlin Spring Boot 백엔드를 컨테이너로 빌드하고 실행합니다.
# Gradle wrapper가 아직 없으므로 공식 Gradle 이미지를 빌드 스테이지로 사용합니다.
FROM gradle:8.8-jdk21-alpine AS build

WORKDIR /workspace
COPY settings.gradle.kts ./
COPY backend ./backend
RUN gradle :backend:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /workspace/backend/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

