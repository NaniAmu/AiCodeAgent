# Production Deployment Guide

## Prerequisites

- Ubuntu 22.04 LTS (or compatible Linux server)
- Docker Engine 24.0+
- Docker Compose 2.20+
- Domain name pointing to your server
- Ports 80 and 443 open in firewall

## Quick Start

### 1. Server Setup

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
sudo apt install -y docker.io docker-compose-plugin

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker compose version
```

### 2. Firewall Configuration

```bash
# Allow necessary ports
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw enable
```

### 3. Deploy Application

```bash
# Clone repository
git clone <your-repo-url> /opt/diginest
cd /opt/diginest

# Create environment file
cp .env.example .env
nano .env  # Edit with your values

# Run deployment script
chmod +x deploy.sh
./deploy.sh
```

## Manual Deployment Steps

### Step 1: Prepare Environment

```bash
# Create .env file
cat > .env << 'EOF'
DB_NAME=diginest
DB_USERNAME=postgres
DB_PASSWORD=$(openssl rand -base64 32)
JWT_SECRET=$(openssl rand -base64 64)
DOMAIN=api.yourdomain.com
EMAIL=admin@yourdomain.com
EOF
```

### Step 2: Initial Deployment (HTTP only)

```bash
# Build and start services
docker-compose -f docker-compose.prod.yml up -d --build

# Verify services are running
docker-compose -f docker-compose.prod.yml ps
docker-compose -f docker-compose.prod.yml logs -f app
```

### Step 3: Setup SSL Certificate

```bash
# Run certbot for initial certificate
docker run -it --rm \
  -v ./certbot-data:/etc/letsencrypt \
  -v ./certbot-www:/var/www/certbot \
  -p 80:80 \
  certbot/certbot certonly \
  --standalone \
  --preferred-challenges http \
  --agree-tos \
  --email admin@yourdomain.com \
  -d api.yourdomain.com
```

### Step 4: Enable HTTPS

Edit `nginx/conf.d/app.conf`:
1. Comment out the HTTP-only server block (lines 1-21)
2. Uncomment the HTTPS server block (lines 24-89)
3. Replace `api.diginest.com` with your domain

```bash
# Reload nginx
docker-compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_NAME` | Yes | diginest | Database name |
| `DB_USERNAME` | Yes | postgres | Database user |
| `DB_PASSWORD` | Yes | - | Database password (strong) |
| `JWT_SECRET` | Yes | - | JWT signing secret (min 32 chars) |
| `JWT_EXPIRATION_MS` | No | 86400000 | Token expiration (24h) |
| `DOMAIN` | Yes | - | Your domain name |
| `EMAIL` | Yes | - | Admin email for SSL |

## Management Commands

### View Logs

```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f app
docker-compose -f docker-compose.prod.yml logs -f postgres
```

### Scale/Rebuild

```bash
# Rebuild and restart
docker-compose -f docker-compose.prod.yml up -d --build --force-recreate

# Restart single service
docker-compose -f docker-compose.prod.yml restart app
```

### Database Backup

```bash
# Create backup
docker-compose -f docker-compose.prod.yml exec postgres \
  pg_dump -U postgres diginest > backup_$(date +%Y%m%d_%H%M%S).sql

# Restore backup
docker-compose -f docker-compose.prod.yml exec -T postgres \
  psql -U postgres diginest < backup_file.sql
```

### Update Application

```bash
# Pull latest code
git pull origin main

# Rebuild and restart
docker-compose -f docker-compose.prod.yml up -d --build
```

## SSL Certificate Renewal

Certbot container automatically renews certificates. To manually renew:

```bash
docker-compose -f docker-compose.prod.yml exec certbot \
  certbot renew --force-renewal

docker-compose -f docker-compose.prod.yml exec nginx nginx -s reload
```

## Troubleshooting

### Service Won't Start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs app

# Check container status
docker-compose -f docker-compose.prod.yml ps

# Restart with clean slate
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### Database Connection Issues

```bash
# Test database connection
docker-compose -f docker-compose.prod.yml exec postgres \
  pg_isready -U postgres

# Reset database (WARNING: deletes all data)
docker volume rm diginest_postgres_data
```

### SSL Certificate Issues

```bash
# Check certificate status
docker-compose -f docker-compose.prod.yml exec certbot \
  certbot certificates

# Recreate certificate
docker-compose -f docker-compose.prod.yml down
docker volume rm diginest_certbot-data
docker-compose -f docker-compose.prod.yml up -d
```

### 502 Bad Gateway

```bash
# Check if app is healthy
docker-compose -f docker-compose.prod.yml ps

# Check app logs
docker-compose -f docker-compose.prod.yml logs app | tail -50

# Restart app
docker-compose -f docker-compose.prod.yml restart app
```

## Security Checklist

- [ ] Changed all default passwords
- [ ] Generated strong JWT_SECRET (32+ characters)
- [ ] Enabled HTTPS/SSL
- [ ] Configured firewall (UFW)
- [ ] Disabled root SSH login
- [ ] Set up automated backups
- [ ] Enabled fail2ban (recommended)
- [ ] Regular security updates

## Monitoring

### Health Check Endpoint

```bash
curl http://localhost:8080/actuator/health
curl https://api.yourdomain.com/actuator/health
```

### Resource Usage

```bash
# Container stats
docker stats

# System resources
htop
```

## Rollback Procedure

```bash
# Revert to previous version
git log --oneline -10
git checkout <previous-commit>
docker-compose -f docker-compose.prod.yml up -d --build
```

## Support

For issues or questions:
1. Check logs: `docker-compose -f docker-compose.prod.yml logs`
2. Review this guide
3. Check GitHub Issues

---

**Last Updated:** 2024-02-23  
**Version:** 1.0.0
