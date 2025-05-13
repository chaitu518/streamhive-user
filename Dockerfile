# ---------- Build Stage ----------
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper and config
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY src ./src

# Build the app
RUN ./gradlew build -x test

# ---------- Runtime Stage ----------
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
