#!/bin/bash

# Function to open a URL in the default browser
open_url() {
  local url=$1
  case "$(uname -s)" in
    Linux*)   xdg-open "$url" ;;
    Darwin*)  open "$url" ;;
    CYGWIN*|MINGW*|MSYS*) explorer.exe "$url" ;;
    *)        echo "Unsupported OS. Please open the URL manually: $url" ;;
  esac
}

# Run Maven build
echo "Building the project with 'mvn clean install'..."
./mvnw clean install

# Check if the build was successful
if [ $? -ne 0 ]; then
  echo "Maven build failed. Aborting."
  exit 1
fi

# Start the application in the background and open the browser
echo "Starting the application..."
./mvnw spring-boot:run -pl holiday-guard-app &

# Give the application some time to start up
echo "Waiting for the application to start..."
sleep 3

# Open the browser
echo "Opening the admin UI in your browser..."
open_url http://localhost:8080/
