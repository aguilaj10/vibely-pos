# Vibely POS Production Deployment Guide

This guide covers deploying the Vibely POS system to production. It covers server setup, database configuration, application deployment, security hardening, and monitoring. Follow these steps to deploy a secure and reliable production environment.

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Server Setup](#2-server-setup)
3. [Database Setup](#3-database-setup)
4. [Backend Deployment](#4-backend-deployment)
5. [Frontend Deployment](#5-frontend-deployment)
6. [Environment Configuration](#6-environment-configuration)
7. [Security Hardening](#7-security-hardening)
8. [SSL/TLS Setup](#8-ssltls-setup)
9. [Monitoring and Logging](#9-monitoring-and-logging)
10. [Backup and Disaster Recovery](#10-backup-and-disaster-recovery)
11. [Troubleshooting](#11-troubleshooting)
12. [Updates and Upgrades](#12-updates-and-upgrades)

---

## 1. Prerequisites

### 1.1 System Requirements

The production server requires the following:

- **Operating System**: Ubuntu 22.04 LTS (recommended)
- **CPU**: 2+ cores
- **Memory**: 4GB+ RAM
- **Disk**: 20GB+ available space
- **Network**: Static IP address with domain name pointed to it

### 1.2 Software Requirements

Install the required software on your server:

```bash
# Update package lists
sudo apt update

# Install essential utilities
sudo apt install -y curl wget git unzip software-properties-common

# Install JDK 17 (required for Ktor backend)
sudo apt install -y openjdk-17-jdk

# Verify Java installation
java -version
# Output should show: openjdk version "17.x.x"

# Install Nginx (reverse proxy)
sudo apt install -y nginx

# Install Certbot for SSL certificates
sudo apt install -y certbot python3-certbot-nginx

# Install logrotate for log management
sudo apt install -y logrotate

# Install monitoring tools
sudo apt install -y htop sysstat
```

### 1.3 Supabase Requirements

You need a Supabase project with:

- PostgreSQL database created
- Service role key (from Settings > API)
- Project URL (format: `https://your-project-id.supabase.co`)

### 1.4 Domain Requirements

- A registered domain name pointing to your server IP
- For SSL, you will use Let's Encrypt (free)

---

## 2. Server Setup

### 2.1 Create Deployment User

Create a dedicated user for running the application:

```bash
# Create application user
sudo adduser vibely

# Add to sudo group (for administrative tasks only)
sudo usermod -aG sudo vibely

# Create application directory
sudo mkdir -p /opt/vibely
sudo chown vibely:vibely /opt/vibely
```

### 2.2 Configure Firewall

Set up UFW firewall rules:

```bash
# Enable UFW
sudo ufw enable

# Allow SSH (limit connections to prevent brute force)
sudo ufw limit 22/tcp

# Allow HTTP and HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Allow backend port (only from localhost if using reverse proxy)
# For direct access (not recommended for production):
# sudo ufw allow 8080/tcp

# Check firewall status
sudo ufw status verbose
```

### 2.3 Configure Timezone and NTP

Set the correct timezone and ensure system time is synchronized:

```bash
# Set timezone
sudo timedatectl set-timezone America/New_York

# Enable NTP
sudo timedatectl set-ntp on

# Verify time sync
timedatectl
```

### 2.4 Configure System Limits

Edit `/etc/security/limits.conf` to increase file descriptor limits:

```bash
sudo nano /etc/security/limits.conf
```

Add these lines at the end:

```
vibely soft nofile 65536
vibely hard nofile 65536
vibely soft nproc 4096
vibely hard nproc 4096
```

Edit `/etc/sysctl.conf` for network optimization:

```bash
sudo nano /etc/sysctl.conf
```

Add these lines:

```
# Network optimization
net.core.somaxconn = 65535
net.ipv4.tcp_max_syn_backlog = 65536
net.ipv4.ip_local_port_range = 1024 65535

# File descriptor limits
fs.file-max = 65536
```

Apply changes:

```bash
sudo sysctl -p
```

---

## 3. Database Setup

### 3.1 Supabase Project Setup

1. Go to [Supabase Dashboard](https://app.supabase.com/)
2. Create a new project or select existing project
3. Navigate to Settings > API
4. Note down:
   - Project URL (format: `https://xxxxx.supabase.co`)
   - Service role key (hidden by default, click to reveal)

### 3.2 Database Migrations

If you have migration files, apply them to your Supabase project. You can use the Supabase CLI:

```bash
# Install Supabase CLI
sudo npm install -g supabase

# Login to Supabase
supabase login

# Link to your project
cd /opt/vibely
supabase link --project-ref YOUR_PROJECT_REF

# Push migrations
supabase db push
```

Alternatively, run SQL migrations via the Supabase SQL Editor in the dashboard:

```sql
-- Apply your migration SQL here
-- Example: Create tables, indexes, RLS policies
```

### 3.3 Database Connection Test

Test the database connection from your server:

```bash
# Install PostgreSQL client
sudo apt install -y postgresql-client

# Test connection (using project URL)
psql "host=db.YOUR_PROJECT_ID.supabase.co port=5432 dbname=postgres user=postgres password=YOUR_PASSWORD"
```

### 3.4 Connection Pooling

For production, configure connection pooling to avoid overwhelming the database. Supabase provides built-in PgBouncer:

1. Go to Supabase Dashboard > Settings > Database
2. Enable Connection Pooler
3. Note the pooler host and port

---

## 4. Backend Deployment

### 4.1 Build the Backend JAR

Build the production JAR on your local machine or CI/CD system:

```bash
# Clone the repository
git clone https://github.com/your-repo/vibely-pos.git
cd vibely-pos

# Build the shadow JAR (includes all dependencies)
./gradlew :backend:shadowJar

# The JAR will be at: backend/build/libs/backend-*-all.jar
```

Transfer the JAR to the server:

```bash
# Copy JAR to server
scp backend/build/libs/backend-1.0.0-all.jar vibely@YOUR_SERVER_IP:/opt/vibely/

# Copy environment file
scp .env.production vibely@YOUR_SERVER_IP:/opt/vibely/.env
```

### 4.2 Create Systemd Service

Create a systemd service file:

```bash
sudo nano /etc/systemd/system/vibely-backend.service
```

Add the following content:

```ini
[Unit]
Description=Vibely POS Backend API
Documentation=https://github.com/your-repo/vibely-pos
After=network.target

[Service]
Type=simple
User=vibely
WorkingDirectory=/opt/vibely

# Environment file
EnvironmentFile=/opt/vibely/.env

# Java options for production
Environment=JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
Environment=JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ExecStart=/usr/bin/java $JAVA_OPTS -jar /opt/vibely/backend-1.0.0-all.jar

# Restart policy
Restart=always
RestartSec=10

# Logging
StandardOutput=append:/opt/vibely/logs/stdout.log
StandardError=append:/opt/vibely/logs/stderr.log

# Security hardening
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadWritePaths=/opt/vibely

[Install]
WantedBy=multi-user.target
```

Create log directory:

```bash
sudo mkdir -p /opt/vibely/logs
sudo chown -R vibely:vibely /opt/vibely
```

### 4.3 Enable and Start Service

```bash
# Reload systemd to recognize the new service
sudo systemctl daemon-reload

# Enable service to start on boot
sudo systemctl enable vibely-backend

# Start the service
sudo systemctl start vibely-backend

# Check service status
sudo systemctl status vibely-backend

# View logs
sudo journalctl -u vibely-backend -f
```

### 4.4 Reverse Proxy Configuration

#### Option A: Nginx

Create Nginx configuration:

```bash
sudo nano /etc/nginx/sites-available/vibely-backend
```

Add the following:

```nginx
upstream vibely_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name api.yourdomain.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    # SSL configuration
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_session_tickets off;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
    ssl_prefer_server_ciphers off;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;
    limit_req zone=api_limit burst=20 nodelay;

    location / {
        proxy_pass http://vibely_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Connection handling
        proxy_set_header Connection "";
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Buffering
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 16k;
        proxy_busy_buffers_size 24k;
    }

    # Health check endpoint (no rate limiting)
    location /health {
        proxy_pass http://vibely_backend/health;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        limit_req zone=none;
    }
}
```

Enable the site:

```bash
sudo ln -s /etc/nginx/sites-available/vibely-backend /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

#### Option B: Caddy

Create Caddyfile:

```bash
sudo nano /etc/caddy/Caddyfile
```

Add the following:

```
api.yourdomain.com {
    reverse_proxy localhost:8080 {
        header_up X-Real-IP {remote_ip}
        header_up X-Forwarded-For {header.X-Forwarded-For}
        header_up X-Forwarded-Proto {header.X-Forwarded-Proto}
    }

    # Rate limiting
    rate_limit 10r/s

    # Security headers
    header {
        X-Frame-Options "SAMEORIGIN"
        X-Content-Type-Options "nosniff"
        X-XSS-Protection "1; mode=block"
        Referrer-Policy "strict-origin-when-cross-origin"
        -Server
    }

    # Logging
    log {
        output file /var/log/caddy/vibely-access.log
    }
}
```

Validate and reload:

```bash
sudo caddy validate --config /etc/caddy/Caddyfile
sudo systemctl reload caddy
```

---

## 5. Frontend Deployment

### 5.1 Build Desktop Application

Build the desktop application for distribution:

```bash
# Build for all desktop platforms
./gradlew :composeApp:packageReleaseForCurrentOS

# Or build specific platforms:
./gradlew :composeApp:packageReleaseForLinux
./gradlew :composeApp:packageReleaseForWindows
./gradlew :composeApp:packageReleaseForMac
```

The built installers will be located in:
- Linux: `composeApp/build/compose/binaries/main/release/`
- Windows: `composeApp/build/compose/binaries/main/release/`
- Mac: `composeApp/build/compose/binaries/main/release/`

### 5.2 Linux Deployment (DEB Package)

For Ubuntu/Debian systems:

```bash
# Copy DEB package to server
scp composeApp/build/compose/binaries/main/release/*-1.0.0.deb vibely@YOUR_SERVER_IP:/tmp/

# Install on server
sudo dpkg -i /tmp/vibely-pos-1.0.0.deb

# Or use apt for dependency resolution
sudo apt install -f
```

Create a desktop launcher:

```bash
sudo nano /usr/share/applications/vibely-pos.desktop
```

Add the following:

```ini
[Desktop Entry]
Name=Vibely POS
Comment=Point of Sale System
Exec=/opt/vibely-pos/bin/vibely-pos
Icon=/opt/vibely-pos/lib/vibely-pos.png
Terminal=false
Type=Application
Categories=Office;Finance;
```

### 5.3 Windows Deployment

For Windows deployment:

1. Use the built MSI or EXE installer
2. Or use portable ZIP distribution:
   - Copy to `C:\Program Files\Vibely POS\`
   - Create Start Menu shortcut
   - Configure auto-start if needed

### 5.4 Configuration

The frontend connects to the backend API. Configure the API URL:

For desktop, create or edit the configuration file in the app data directory, or set it via environment variable:

```bash
# Linux
export VIBELY_API_URL=https://api.yourdomain.com

# Windows (System Properties > Environment Variables)
setx VIBELY_API_URL "https://api.yourdomain.com"
```

---

## 6. Environment Configuration

### 6.1 Production Environment Variables

Create the production environment file:

```bash
sudo nano /opt/vibely/.env
```

Add the following:

```env
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key-here

# JWT Configuration (generate a strong random string)
JWT_SECRET=your-secure-jwt-secret-min-32-chars-long-abc123xyz789

# Server Configuration
PORT=8080
HOST=0.0.0.0

# Security Configuration
ENFORCE_HTTPS=true

# Production - MUST be false
DEBUG_MODE=false
```

### 6.2 Generate Secure JWT Secret

Generate a secure JWT secret:

```bash
# Generate 256-bit (32 character) random string
openssl rand -base64 32 | tr -d '\n'
```

### 6.3 Secrets Management

For enhanced security, consider using a secrets manager:

```bash
# Option: Use systemd secret storage
# Edit the service file and use Environment=SECRET=value syntax

# Option: Use HashiCorp Vault (recommended for enterprise)
# Install vault and configure for secret management

# Option: Use AWS Secrets Manager (if on AWS)
```

---

## 7. Security Hardening

### 7.1 Security Checklist

Complete all items in this checklist before going to production:

- [ ] SSL/TLS certificate installed and working
- [ ] Firewall configured (only ports 80, 443, 22 open)
- [ ] SSH key-based authentication enabled
- [ ] Root login disabled in SSH config
- [ ] JWT secret is unique and secure (32+ characters)
- [ ] Supabase service role key is kept secret
- [ ] DEBUG_MODE set to false
- [ ] ENFORCE_HTTPS set to true
- [ ] Security headers configured in reverse proxy
- [ ] Rate limiting enabled
- [ ] Failed login attempt limiting enabled
- [ ] Database RLS policies enabled in Supabase
- [ ] Regular security updates scheduled
- [ ] Backup encryption enabled
- [ ] Log monitoring set up

### 7.2 SSH Hardening

Edit SSH configuration:

```bash
sudo nano /etc/ssh/sshd_config
```

Apply these settings:

```
# Disable root login
PermitRootLogin no

# Disable password authentication
PasswordAuthentication no

# Use key-based authentication only
PubkeyAuthentication yes

# Change default port
Port 2222

# Limit login attempts
MaxAuthTries 3

# Banner
Banner /etc/ssh/banner
```

Create a banner:

```bash
sudo nano /etc/ssh/banner
```

Add your warning message:

```
UNAUTHORIZED ACCESS PROHIBITED
All access is monitored and logged.
```

Restart SSH:

```bash
sudo systemctl restart sshd

# Update firewall for new SSH port
sudo ufw limit 2222/tcp
```

### 7.3 Disable Unused Services

```bash
# List running services
systemctl list-unit-files --state=enabled

# Disable unnecessary services
sudo systemctl disable bluetooth
sudo systemctl disable cups
```

### 7.4 Intrusion Prevention

Install and configure Fail2Ban:

```bash
sudo apt install -y fail2ban

sudo nano /etc/fail2ban/jail.local
```

Add the following:

```
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 3

[sshd]
enabled = true
port = 2222

[nginx-http-auth]
enabled = true

[nginx-noscript]
enabled = true
```

Start Fail2Ban:

```bash
sudo systemctl enable fail2ban
sudo systemctl start fail2ban
```

---

## 8. SSL/TLS Setup

### 8.1 Let's Encrypt with Certbot

Obtain free SSL certificate from Let's Encrypt:

```bash
# Stop Nginx (required for standalone certbot)
sudo systemctl stop nginx

# Obtain certificate
sudo certbot certonly --standalone -d api.yourdomain.com

# Or use nginx plugin (if Nginx is running)
sudo certbot --nginx -d api.yourdomain.com
```

### 8.2 Certificate Auto-Renewal

Certbot sets up auto-renewal by default. Verify:

```bash
# Check renewal timer
sudo systemctl list-timers | grep certbot

# Test renewal
sudo certbot renew --dry-run

# Manual renewal if needed
sudo certbot renew
```

Add to crontab for verification:

```bash
sudo crontab -e
```

Add this line:

```
0 3 * * * /usr/bin/certbot renew --quiet --deploy-hook "systemctl reload nginx"
```

### 8.3 SSL Configuration Test

Test your SSL configuration:

```bash
# Install test tool
sudo apt install -y sslscan

# Test your domain
sslscan api.yourdomain.com

# Or use SSL Labs
# Visit: https://www.ssllabs.com/ssltest/analyze.html?d=api.yourdomain.com
```

---

## 9. Monitoring and Logging

### 9.1 Application Logging

Configure log rotation:

```bash
sudo nano /etc/logrotate.d/vibely
```

Add the following:

```
/opt/vibely/logs/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 0640 vibely vibely
    sharedscripts
    postrotate
        systemctl reload vibely-backend > /dev/null 2>&1 || true
    endscript
}
```

### 9.2 Log Monitoring

Set up log monitoring with the ELK stack or simpler alternatives:

#### Option A: Centralized Logging with rsyslog

```bash
sudo apt install -y rsyslog

# Configure rsyslog to send logs to central server
sudo nano /etc/rsyslog.conf
```

Add remote logging:

```
*.* @your-log-server:514
```

#### Option B: Simple Log Monitoring

Install logwatch for daily log summaries:

```bash
sudo apt install -y logwatch

sudo nano /etc/cron.daily/00logwatch
```

Add:

```bash
/usr/sbin/logwatch --range yesterday --mailto admin@yourdomain.com --detail high
```

### 9.3 Health Checks

Set up health check monitoring:

```bash
# Create health check script
nano /opt/vibely/health-check.sh
```

Add the following:

```bash
#!/bin/bash

# Check backend health
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" https://api.yourdomain.com/health)

if [ "$BACKEND_STATUS" != "200" ]; then
    echo "Backend health check failed: HTTP $BACKEND_STATUS"
    # Send alert (email, Slack, etc.)
    exit 1
fi

echo "Backend is healthy"
exit 0
```

Make executable and add to crontab:

```bash
chmod +x /opt/vibely/health-check.sh
sudo crontab -e
```

Add line:

```
*/5 * * * * /opt/vibely/health-check.sh >> /var/log/health-check.log 2>&1
```

### 9.4 Uptime Monitoring

Consider using these services for external monitoring:

- UptimeRobot (free tier available)
- Pingdom
- Datadog
- Prometheus + Grafana

Example Prometheus configuration:

```yaml
# /etc/prometheus/vibely.yml
- job_name: 'vibely-backend'
  static_configs:
    - targets: ['localhost:8080']
  metrics_path: '/health'
```

### 9.5 Resource Monitoring

Set up monitoring with Netdata or Prometheus:

```bash
# Install Netdata
bash <(curl -Ss https://my-netdata.io/kickstart.sh)
```

Access at `http://your-server:19999`

---

## 10. Backup and Disaster Recovery

### 10.1 Database Backups

Set up automatic Supabase database backups:

1. In Supabase Dashboard, go to Settings > Database
2. Enable Point in Time Recovery (PITR)
3. Or use the Supabase CLI for manual backups:

```bash
# Create manual backup
supabase db remote commit

# List backups
supabase db list
```

### 10.2 Application Backup Script

Create backup script:

```bash
sudo nano /opt/vibely/backup.sh
```

Add the following:

```bash
#!/bin/bash

# Configuration
BACKUP_DIR="/opt/vibely/backups"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# Create backup directory
mkdir -p $BACKUP_DIR

# Backup application files
tar -czf $BACKUP_DIR/vibely-app-$DATE.tar.gz \
    /opt/vibely/*.jar \
    /opt/vibely/.env \
    /opt/vibely/logs

# Backup configuration
tar -czf $BACKUP_DIR/vibely-config-$DATE.tar.gz \
    /etc/nginx/sites-available/vibely-backend \
    /etc/systemd/system/vibely-backend.service

# Remove old backups
find $BACKUP_DIR -name "vibely-*.tar.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: $DATE"
```

Make executable:

```bash
chmod +x /opt/vibely/backup.sh
```

Add to crontab:

```bash
sudo crontab -e
```

Add line:

```
0 2 * * * /opt/vibely/backup.sh >> /var/log/vibely-backup.log 2>&1
```

### 10.3 Off-Site Backup

Sync backups to cloud storage:

```bash
# Install rclone
curl https://rclone.org/install.sh | sudo bash

# Configure rclone (interactive)
rclone config

# Add to backup script for automatic sync
# rclone sync /opt/vibely/backups remote:bucket --exclude "*.tmp"
```

### 10.4 Disaster Recovery Plan

Document your recovery procedures:

1. **Database Recovery**:
   - Go to Supabase Dashboard
   - Navigate to Database > Point in Time Recovery
   - Select restore point
   - Confirm restoration

2. **Application Recovery**:
   - Access server via SSH
   - Pull latest deployment
   - Restore configuration from backup
   - Restart services

3. **Full Server Recovery**:
   - Provision new server
   - Apply security hardening
   - Restore application from backup
   - Restore SSL certificates
   - Verify all services

### 10.5 Testing Backups

Regularly test your backup restoration process:

```bash
# Test backup extraction
tar -tzf /opt/vibely/backups/vibely-app-20240115_020000.tar.gz

# Document restoration steps
# Practice restoration on test environment
```

---

## 11. Troubleshooting

### 11.1 Common Issues

#### Backend Won't Start

Check logs:

```bash
sudo journalctl -u vibely-backend -n 100 --no-pager
```

Common causes:

- Missing environment variables
- Port already in use: `sudo lsof -i :8080`
- Invalid JAR file: verify file size and re-upload
- Memory issues: check available RAM

#### Database Connection Failed

Verify credentials:

```bash
# Test from server
curl -s https://api.yourdomain.com/api/test/database

# Check Supabase project status
# Go to Supabase Dashboard > Settings > Health
```

#### SSL Certificate Issues

Renew certificate:

```bash
sudo certbot renew --force-renewal
```

Check certificate paths:

```bash
sudo certbot certificates
```

#### High Memory Usage

Check JVM memory:

```bash
# Check process memory
ps aux | grep java

# Check system memory
free -h

# Adjust JVM heap in service file
# Environment=JAVA_OPTS="-Xms256m -Xmx512m ..."
```

### 11.2 Log Locations

| Log | Location |
|-----|----------|
| Backend stdout | `/opt/vibely/logs/stdout.log` |
| Backend stderr | `/opt/vibely/logs/stderr.log` |
| Nginx access | `/var/log/nginx/access.log` |
| Nginx error | `/var/log/nginx/error.log` |
| System logs | `/var/log/syslog` |
| Auth logs | `/var/log/auth.log` |

### 11.3 Debugging Commands

```bash
# Check service status
sudo systemctl status vibely-backend

# View real-time logs
sudo journalctl -u vibely-backend -f

# Check network connections
sudo netstat -tlnp | grep 8080

# Check processes
ps aux | grep java

# Check resource usage
htop

# Check disk space
df -h

# Check memory
free -m
```

### 11.4 Health Check Verification

Test endpoints manually:

```bash
# Backend health
curl https://api.yourdomain.com/health

# Database test
curl https://api.yourdomain.com/api/test/database
```

---

## 12. Updates and Upgrades

### 12.1 Application Updates

Create an update script:

```bash
sudo nano /opt/vibely/update.sh
```

Add the following:

```bash
#!/bin/bash

set -e

BACKEND_VERSION="1.0.0"
BACKUP_DIR="/opt/vibely/backups"
DATE=$(date +%Y%m%d_%H%M%S)

echo "Starting update process..."

# Backup current version
echo "Backing up current version..."
mkdir -p $BACKUP_DIR
cp /opt/vibely/backend-*.jar $BACKUP_DIR/backend-$DATE.jar || true

# Download new version (replace with your download mechanism)
# Example: curl -o /opt/vibely/backend-new.jar https://build-server/backend-$BACKEND_VERSION.jar

# Verify new JAR (check signature or hash)
# echo "Verifying new JAR..."
# sha256sum /opt/vibely/backend-new.jar

# Deploy new version
echo "Deploying new version..."
# mv /opt/vibely/backend-new.jar /opt/vibely/backend-$BACKEND_VERSION-all.jar

# Restart service
echo "Restarting service..."
sudo systemctl restart vibely-backend

# Wait for startup
sleep 10

# Verify health
echo "Verifying deployment..."
if curl -sf https://api.yourdomain.com/health > /dev/null; then
    echo "Update successful!"
else
    echo "Update failed! Rolling back..."
    cp $BACKUP_DIR/backend-$DATE.jar /opt/vibely/backend-$BACKEND_VERSION-all.jar
    sudo systemctl restart vibely-backend
    exit 1
fi
```

### 12.2 Zero-Downtime Deployment

For zero-downtime updates, use a blue-green deployment:

```bash
# Deploy to alternate port
# Update nginx upstream to point to new instance
# Test new instance
# Switch traffic
# Keep old instance for rollback
```

Update Nginx for blue-green:

```bash
# Add upstream for new instance
# upstream vibely_backend_new {
#     server 127.0.0.1:8081;
# }

# Test new instance on port 8081
# Switch to new upstream
# Rollback by switching back
```

### 12.3 Rollback Procedure

If an update fails:

```bash
# Find backup
ls -la /opt/vibely/backups/

# Restore previous version
sudo systemctl stop vibely-backend
cp /opt/vibely/backups/backend-YYYYMMDD_HHMMSS.jar /opt/vibely/backend-1.0.0-all.jar

# Start service
sudo systemctl start vibely-backend

# Verify
curl https://api.yourdomain.com/health
```

### 12.4 System Updates

Regularly update system packages:

```bash
# Update package lists
sudo apt update

# Install security updates
sudo apt upgrade -y

# Remove unused packages
sudo apt autoremove -y

# Reboot if needed
sudo reboot
```

Schedule automatic security updates:

```bash
sudo apt install -y unattended-upgrades

sudo dpkg-reconfigure -plow unattended-upgrades
```

---

## Quick Reference

### Service Management Commands

```bash
# Start service
sudo systemctl start vibely-backend

# Stop service
sudo systemctl stop vibely-backend

# Restart service
sudo systemctl restart vibely-backend

# View status
sudo systemctl status vibely-backend

# View logs
sudo journalctl -u vibely-backend -f
```

### Nginx Commands

```bash
# Test configuration
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx

# Restart nginx
sudo systemctl restart nginx
```

### Useful URLs

| Service | URL |
|---------|-----|
| Backend Health | `https://api.yourdomain.com/health` |
| Database Test | `https://api.yourdomain.com/api/test/database` |
| Nginx Status | `http://localhost/nginx_status` |
| Netdata | `http://your-server:19999` |

---

## Support

For deployment issues:

1. Check logs first: `sudo journalctl -u vibely-backend -n 100`
2. Verify all environment variables are set
3. Test database connectivity
4. Check SSL certificate status
5. Review security checklist

For general support, open an issue on GitHub.

---

**Document Version**: 1.0  
**Last Updated**: March 2026  
**Next Review**: Monthly
