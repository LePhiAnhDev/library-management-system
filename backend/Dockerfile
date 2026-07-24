# syntax=docker/dockerfile:1

# ============================================================================
# Builder: build the Spring Boot fat jar with Maven.
# Tests are skipped here on purpose: the integration tests use Testcontainers
# which needs a Docker daemon inside the build (not available). CI runs them
# separately (.github/workflows/ci.yml).
# ============================================================================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Cache dependencies first (only re-runs when pom.xml changes).
COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

# Build the jar.
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# ============================================================================
# Production: slim JRE running the jar.
# Flyway (spring-boot-starter-flyway) applies db/migration/V1..V13 automatically
# on startup, so there is NO separate migration command.
# ============================================================================
FROM eclipse-temurin:21-jre-alpine AS production
WORKDIR /app

# wget is used by the container healthcheck; run as a non-root user.
RUN apk add --no-cache wget \
    && addgroup -S app && adduser -S -G app app

# Production config is baked in: Spring reads ./.env via spring.config.import.
COPY .env.production ./.env
COPY --from=builder /build/target/library-0.0.1-SNAPSHOT.jar ./app.jar

RUN chown -R app:app /app
USER app

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Exec form => java is PID 1, so SIGTERM triggers Spring graceful shutdown.
CMD ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
