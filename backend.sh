#!/bin/bash
echo "Starting Holiday Guard Backend Application..."
./mvnw spring-boot:run -pl holiday-guard-app -Dspring-boot.run.profiles=local
