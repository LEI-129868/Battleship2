# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Use a slim version of Java 21 (or your preferred version)
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy pom.xml first so Maven dependency layer is cached between builds
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat JAR (skipping tests for the image build)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the executable JAR from the build stage
COPY --from=build /app/target/BattleshipGamePlayer-2.0.jar app.jar

# Expose the REST server port (the port your server listens on)
EXPOSE 8080

# Runs the app, enabling assertions for better debugging
ENTRYPOINT ["java", "-ea", "-jar", "app.jar"]
