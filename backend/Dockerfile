# Kotlin Spring Boot 백엔드를 컨테이너로 빌드하고 실행합니다.
# Gradle 빌드는 아키텍처 독립적인 jar를 만들기 때문에 빌드 플랫폼에서만 수행합니다.
ARG BUILDPLATFORM=linux/amd64
ARG TARGETPLATFORM=linux/amd64
FROM --platform=$BUILDPLATFORM gradle:8.8-jdk21-alpine AS build

WORKDIR /workspace
COPY settings.gradle.kts ./
COPY backend ./backend
RUN gradle :backend:bootJar --no-daemon

FROM --platform=$TARGETPLATFORM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /workspace/backend/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
