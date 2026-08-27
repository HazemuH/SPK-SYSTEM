# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# The cache mount keeps ~/.m2 outside the layer cache, so a pom.xml or source
# change re-resolves only what actually changed instead of the whole tree.
# maven.test.skip (not skipTests) also skips *compiling* tests, so the test-only
# dependencies are never downloaded into this runtime image.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -Dmaven.test.skip=true clean package

# ---- run stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Datasource / secret come from the environment (see docker-compose.yml).
ENTRYPOINT ["java", "-jar", "app.jar"]
