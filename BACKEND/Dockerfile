# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Warm the dependency cache (best-effort; package below re-resolves if needed).
RUN mvn -B -q dependency:go-offline -DskipTests || true
COPY src ./src
RUN mvn -B -q -DskipTests clean package

# ---- run stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Datasource / secret come from the environment (see docker-compose.yml).
ENTRYPOINT ["java", "-jar", "app.jar"]
