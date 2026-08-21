# syntax=docker/dockerfile:1

# ---- Build stage --------------------------------------------------------
# The official Maven image (not the wrapper) keeps the build reproducible
# regardless of what committed into .mvn/wrapper.
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Dependencies are resolved before the source is copied so a source-only
# change doesn't invalidate the (large) dependency layer.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage --------------------------------------------------------
# JRE, not JDK: nothing here compiles code, so the smaller runtime is enough.
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S dex && adduser -S dex -G dex
WORKDIR /app
COPY --from=build /workspace/target/insights-*.jar app.jar
RUN chown dex:dex app.jar
USER dex

EXPOSE 8080

# Reads the same actuator endpoint ECS/Kubernetes would probe, so a container
# that fails locally fails the same way it would in orchestration.
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
