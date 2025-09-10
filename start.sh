#!/usr/bin/env bash

echo "Starting Holiday Guard Application..."
echo "Building project and starting Spring Boot application..."

# Run the application from the app module using root mvnw wrapper
./mvnw spring-boot:run -pl holiday-guard-app