#!/bin/bash

# DigiNest AI Receptionist - Test Runner Script
# Usage: ./run-tests.sh [options]

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "DigiNest AI Receptionist - Test Suite"
echo "========================================"
echo ""

# Check if Maven wrapper exists
if [ ! -f "./mvnw" ]; then
    echo -e "${RED}Error: Maven wrapper not found. Run from project root.${NC}"
    exit 1
fi

# Make mvnw executable
chmod +x ./mvnw

# Parse arguments
TEST_CLASS=""
VERBOSE=""
SKIP_CLEAN=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --auth)
            TEST_CLASS="AuthControllerIntegrationTest"
            shift
            ;;
        --booking)
            TEST_CLASS="BookingControllerIntegrationTest"
            shift
            ;;
        --usage)
            TEST_CLASS="UsageControllerIntegrationTest"
            shift
            ;;
        --suite)
            TEST_CLASS="IntegrationTestSuite"
            shift
            ;;
        --verbose|-v)
            VERBOSE="-X"
            shift
            ;;
        --skip-clean)
            SKIP_CLEAN=true
            shift
            ;;
        --help|-h)
            echo "Usage: ./run-tests.sh [options]"
            echo ""
            echo "Options:"
            echo "  --auth        Run only Authentication API tests"
            echo "  --booking     Run only Booking API tests"
            echo "  --usage       Run only Usage Tracking API tests"
            echo "  --suite       Run full test suite (default)"
            echo "  --verbose, -v Enable verbose Maven output"
            echo "  --skip-clean  Skip 'clean' phase (faster)"
            echo "  --help, -h    Show this help message"
            echo ""
            echo "Examples:"
            echo "  ./run-tests.sh              # Run all tests"
            echo "  ./run-tests.sh --auth       # Run auth tests only"
            echo "  ./run-tests.sh --booking -v # Run booking tests with verbose output"
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            echo "Run './run-tests.sh --help' for usage information"
            exit 1
            ;;
    esac
done

# Default to running all tests if no specific class selected
if [ -z "$TEST_CLASS" ]; then
    TEST_CLASS="IntegrationTestSuite"
    echo -e "${YELLOW}Running full test suite...${NC}"
else
    echo -e "${YELLOW}Running: $TEST_CLASS${NC}"
fi

echo ""
echo "Step 1: Cleaning previous builds..."
if [ "$SKIP_CLEAN" = false ]; then
    ./mvnw clean -q
    echo -e "${GREEN}✓ Clean completed${NC}"
else
    echo -e "${YELLOW}⚠ Skipped (using --skip-clean)${NC}"
fi

echo ""
echo "Step 2: Running tests..."
./mvnw test $VERBOSE -Dtest=$TEST_CLASS -Dspring.profiles.active=test

# Check test results
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}All tests passed successfully!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo "Test reports available at:"
    echo "  target/surefire-reports/"
    exit 0
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}Tests failed!${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo "Check test reports for details:"
    echo "  target/surefire-reports/"
    exit 1
fi
