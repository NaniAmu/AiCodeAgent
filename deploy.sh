#!/bin/bash

# DigiNest AI Receptionist - Production Deployment Script
# Usage: ./deploy.sh [environment]

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}DigiNest AI Receptionist Deployment${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Check if running as root
if [ "$EUID" -eq 0 ]; then
   echo -e "${RED}Error: Do not run as root${NC}"
   exit 1
fi

# Check if docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    exit 1
fi

# Check if docker compose is available
if ! docker compose version &> /dev/null; then
    echo -e "${RED}Error: Docker Compose is not installed${NC}"
    exit 1
fi

# Check if .env file exists
if [ ! -f "$ENV_FILE" ]; then
    echo -e "${YELLOW}Warning: .env file not found${NC}"
    if [ -f ".env.example" ]; then
        echo -e "${YELLOW}Creating .env from .env.example${NC}"
        cp .env.example .env
        echo -e "${RED}Please edit .env with your actual values before deploying${NC}"
        exit 1
    else
        echo -e "${RED}Error: Neither .env nor .env.example found${NC}"
        exit 1
    fi
fi

# Check if required environment variables are set
if ! grep -q "DB_PASSWORD=" .env || grep -q "DB_PASSWORD=change_this" .env; then
    echo -e "${RED}Error: Please set a strong DB_PASSWORD in .env${NC}"
    exit 1
fi

if ! grep -q "JWT_SECRET=" .env || grep -q "JWT_SECRET=your_jwt" .env; then
    echo -e "${RED}Error: Please set JWT_SECRET in .env${NC}"
    echo -e "${YELLOW}Generate with: openssl rand -base64 64${NC}"
    exit 1
fi

if ! grep -q "DOMAIN=" .env || grep -q "DOMAIN=api.yourdomain" .env; then
    echo -e "${YELLOW}Warning: DOMAIN not set or using default in .env${NC}"
    echo -e "${YELLOW}SSL certificate will use placeholder domain${NC}"
fi

echo -e "${BLUE}Step 1: Pulling latest images...${NC}"
docker compose -f $COMPOSE_FILE pull 2>/dev/null || true

echo ""
echo -e "${BLUE}Step 2: Building application...${NC}"
docker compose -f $COMPOSE_FILE build --no-cache

echo ""
echo -e "${BLUE}Step 3: Starting services...${NC}"
docker compose -f $COMPOSE_FILE up -d

echo ""
echo -e "${BLUE}Step 4: Waiting for database...${NC}"
sleep 10

# Check if services are running
echo ""
echo -e "${BLUE}Step 5: Checking service status...${NC}"
if docker compose -f $COMPOSE_FILE ps | grep -q "healthy\|Up (healthy)"; then
    echo -e "${GREEN}✓ Services are healthy${NC}"
else
    echo -e "${YELLOW}⚠ Waiting for services to become healthy...${NC}"
    sleep 20
fi

# Display status
echo ""
echo -e "${BLUE}Step 6: Deployment status:${NC}"
docker compose -f $COMPOSE_FILE ps

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment completed successfully!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Get domain from .env if available
DOMAIN=$(grep DOMAIN= .env | cut -d '=' -f2 || echo "localhost")

echo -e "${BLUE}Application URLs:${NC}"
echo "  HTTP:  http://$DOMAIN"
if docker compose -f $COMPOSE_FILE exec nginx test -f /etc/letsencrypt/live/*/fullchain.pem 2>/dev/null; then
    echo "  HTTPS: https://$DOMAIN"
fi
echo ""
echo -e "${BLUE}Management Commands:${NC}"
echo "  View logs:    docker compose -f $COMPOSE_FILE logs -f"
echo "  Restart app:  docker compose -f $COMPOSE_FILE restart app"
echo "  Stop all:     docker compose -f $COMPOSE_FILE down"
echo "  Health check: curl http://localhost:8080/actuator/health"
echo ""
echo -e "${YELLOW}Next Steps:${NC}"
if ! docker compose -f $COMPOSE_FILE exec nginx test -f /etc/letsencrypt/live/*/fullchain.pem 2>/dev/null; then
    echo "  1. Configure DNS to point $DOMAIN to this server"
    echo "  2. Obtain SSL certificate (see DEPLOYMENT.md)"
    echo "  3. Enable HTTPS in nginx configuration"
fi
