#!/bin/bash
# Load environment variables from .env file and run backend

set -a  # automatically export all variables
source .env
set +a

echo "Starting backend with environment variables loaded..."
./gradlew :backend:run
