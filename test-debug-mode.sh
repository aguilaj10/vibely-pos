#!/bin/bash
# Test script to verify debug mode is working

echo "========================================"
echo "Testing Debug Mode Setup"
echo "========================================"
echo ""

# Check if .env has DEBUG_MODE
echo "✓ Checking .env configuration..."
if grep -q "DEBUG_MODE=true" .env; then
    echo "  ✓ DEBUG_MODE=true found in .env"
else
    echo "  ✗ DEBUG_MODE not set in .env"
    exit 1
fi

# Check compilation
echo ""
echo "✓ Checking compilation..."
./gradlew assemble -q > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "  ✓ Project compiles successfully"
else
    echo "  ✗ Compilation failed"
    exit 1
fi

echo ""
echo "========================================"
echo "✅ All checks passed!"
echo "========================================"
echo ""
echo "To run in debug mode:"
echo "1. Start backend:  ./run-backend.sh"
echo "2. Start frontend: ./gradlew :composeApp:run --args=\"--skip-auth\""
echo ""
echo "Expected behavior:"
echo "- Backend accepts 'debug-access-token'"
echo "- Frontend automatically uses mock token"
echo "- Dashboard loads without 401 errors"
