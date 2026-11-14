# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy pom files first — this layer is cached until a pom changes, avoiding
# a full dependency re-download on every source change.
COPY pom.xml .
COPY holiday-guard-domain/pom.xml holiday-guard-domain/
COPY holiday-guard-repository/pom.xml holiday-guard-repository/
COPY holiday-guard-repository-h2/pom.xml holiday-guard-repository-h2/
COPY holiday-guard-repository-json/pom.xml holiday-guard-repository-json/
COPY holiday-guard-core/pom.xml holiday-guard-core/
COPY holiday-guard-rest/pom.xml holiday-guard-rest/
COPY holiday-guard-react/pom.xml holiday-guard-react/
COPY holiday-guard-security-inmemory/pom.xml holiday-guard-security-inmemory/
COPY holiday-guard-cli/pom.xml holiday-guard-cli/
COPY holiday-guard-app/pom.xml holiday-guard-app/
RUN mvn dependency:go-offline --no-transfer-progress -q || true

COPY . .
RUN mvn package -DskipTests --no-transfer-progress

# Stage 2: Runtime
FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app
COPY --from=build /app/holiday-guard-app/target/holiday-guard-app-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
