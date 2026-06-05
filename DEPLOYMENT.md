# 🚀 GUÍA DE DESPLIEGUE - La Esperanza Backend

Guía paso a paso para desplegar la aplicación en un VPS con Docker.

## 📋 Checklist Pre-Despliegue

- [ ] Código en Git (remoto)
- [ ] Todos los tests pasan
- [ ] Variables de entorno configuradas (.env)
- [ ] Base de datos migrada
- [ ] SSL/Certificados listos
- [ ] Dominio apuntando al VPS
- [ ] Backups configurados
- [ ] Monitoreo configurado

## 🖥️ Opción 1: Despliegue Manual en VPS

### Paso 1: Preparar el servidor

```bash
# Conectar a VPS
ssh usuario@tu-vps.com

# Actualizar sistema
sudo apt-get update && sudo apt-get upgrade -y

# Instalar dependencias
sudo apt-get install -y \
  curl \
  wget \
  git \
  htop \
  nano

# Instalar Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
newgrp docker

# Instalar Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Verificar instalación
docker --version
docker-compose --version
```

### Paso 2: Clonar repositorio

```bash
# Crear directorio de la aplicación
mkdir -p ~/apps
cd ~/apps

# Clonar repositorio
git clone https://github.com/usuario/la-esperanza.git
cd la-esperanza/backend

# Si el repo es privado, generar SSH key
ssh-keygen -t ed25519 -C "tu-email@example.com"
# Agregar la clave pública al repo settings
```

### Paso 3: Configurar variables de entorno

```bash
# Crear archivo .env con datos de producción
cp .env.example .env
nano .env

# Configurar:
DB_PASSWORD=tu-password-muy-seguro-aqui
JWT_SECRET=tu-clave-secreta-ultra-segura-minimo-32-caracteres
CORS_ORIGINS=https://laesperanza.com,https://api.laesperanza.com
```

### Paso 4: Configurar SSL con Let's Encrypt

```bash
# Instalar Certbot
sudo apt-get install -y certbot

# Obtener certificado (debe haber puerto 80 abierto)
sudo certbot certonly --standalone \
  -d api.laesperanza.com \
  -d laesperanza.com \
  --non-interactive \
  --agree-tos \
  -m tu-email@example.com

# Certificado se guarda en:
# /etc/letsencrypt/live/tu-dominio/
```

### Paso 5: Iniciar aplicación con Docker Compose

```bash
# Desde directorio backend
docker-compose up -d

# Verificar que los contenedores están corriendo
docker-compose ps

# Ver logs
docker-compose logs -f backend

# Verificar salud
curl http://localhost:8080/api/actuator/health
```

### Paso 6: Configurar Nginx como proxy inverso

```bash
# Instalar Nginx
sudo apt-get install -y nginx

# Crear archivo de configuración
sudo nano /etc/nginx/sites-available/la-esperanza

# Copiar esta configuración:
```

```nginx
# /etc/nginx/sites-available/la-esperanza

# Redirigir HTTP a HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name api.laesperanza.com laesperanza.com;
    
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    
    location / {
        return 301 https://$server_name$request_uri;
    }
}

# HTTPS - Backend API
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name api.laesperanza.com;

    # Certificados SSL
    ssl_certificate /etc/letsencrypt/live/api.laesperanza.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.laesperanza.com/privkey.pem;

    # Configuración SSL
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;

    # Proxy a Backend
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header Connection "upgrade";
        proxy_set_header Upgrade $http_upgrade;
        
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Buffering
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
    }

    # Health check (no loguear)
    location /api/actuator/health {
        proxy_pass http://127.0.0.1:8080;
        access_log off;
    }
}

# Frontend (opcional)
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name laesperanza.com;

    ssl_certificate /etc/letsencrypt/live/laesperanza.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/laesperanza.com/privkey.pem;

    root /var/www/laesperanza/html;
    index index.html;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Cache estático
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

```bash
# Habilitar sitio
sudo ln -s /etc/nginx/sites-available/la-esperanza /etc/nginx/sites-enabled/

# Remover default
sudo rm /etc/nginx/sites-enabled/default

# Verificar sintaxis
sudo nginx -t

# Reiniciar Nginx
sudo systemctl restart nginx
```

### Paso 7: Configurar renovación de certificados automática

```bash
# Crear servicio de renovación
sudo nano /etc/systemd/system/certbot-renew.service

# Agregar:
[Unit]
Description=Renew Let's Encrypt Certificates
After=network-online.target

[Service]
Type=oneshot
ExecStart=/usr/bin/certbot renew --quiet --post-hook "systemctl reload nginx"

[Install]
WantedBy=multi-user.target

# Crear timer
sudo nano /etc/systemd/system/certbot-renew.timer

# Agregar:
[Unit]
Description=Renew Let's Encrypt Certificates Daily

[Timer]
OnCalendar=daily
OnBootSec=60

[Install]
WantedBy=timers.target

# Habilitar
sudo systemctl enable certbot-renew.timer
sudo systemctl start certbot-renew.timer
```

### Paso 8: Configurar backups automáticos

```bash
# Script de backup
sudo nano /usr/local/bin/backup-laesperanza.sh

# Agregar:
#!/bin/bash
BACKUP_DIR="/backups/la-esperanza"
mkdir -p $BACKUP_DIR

# Backup de BD
cd ~/apps/la-esperanza/backend
docker-compose exec -T postgres pg_dump -U postgres la_esperanza | gzip > $BACKUP_DIR/db-$(date +%Y%m%d-%H%M%S).sql.gz

# Backup de archivos
tar -czf $BACKUP_DIR/files-$(date +%Y%m%d-%H%M%S).tar.gz ~/apps/la-esperanza/

# Mantener últimos 7 días
find $BACKUP_DIR -type f -mtime +7 -delete

# Hacer ejecutable
sudo chmod +x /usr/local/bin/backup-laesperanza.sh

# Crear cron job (diario a las 2 AM)
(sudo crontab -l 2>/dev/null; echo "0 2 * * * /usr/local/bin/backup-laesperanza.sh") | sudo crontab -
```

## 🔄 Opción 2: CI/CD con GitHub Actions

Crear `.github/workflows/deploy.yml`:

```yaml
name: Deploy to VPS

on:
  push:
    branches: [main]
    paths:
      - 'backend/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Build Docker image
        run: |
          cd backend
          docker build -t la-esperanza/backend:latest .

      - name: Deploy to VPS
        env:
          SSH_PRIVATE_KEY: ${{ secrets.SSH_PRIVATE_KEY }}
          VPS_HOST: ${{ secrets.VPS_HOST }}
          VPS_USER: ${{ secrets.VPS_USER }}
        run: |
          mkdir -p ~/.ssh
          echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
          chmod 600 ~/.ssh/id_rsa
          ssh-keyscan -H $VPS_HOST >> ~/.ssh/known_hosts

          ssh $VPS_USER@$VPS_HOST << 'EOF'
            cd ~/apps/la-esperanza/backend
            git pull origin main
            docker-compose down
            docker-compose up -d
          EOF
```

## 📊 Monitoreo en Producción

### Verificar salud

```bash
# Health check
curl https://api.laesperanza.com/api/actuator/health

# Métricas
curl https://api.laesperanza.com/api/actuator/metrics

# Ver logs
ssh usuario@vps.com "docker-compose -f ~/apps/la-esperanza/backend/docker-compose.yml logs -f backend"
```

### Alertas (con Uptime Robot)

1. Ir a https://uptimerobot.com
2. Crear monitor para https://api.laesperanza.com/api/actuator/health
3. Configurar alertas por email

## 🔄 Actualizar aplicación

```bash
# SSH a VPS
ssh usuario@tu-vps.com
cd ~/apps/la-esperanza/backend

# Actualizar código
git pull origin main

# Reconstruir y reiniciar
docker-compose down
docker-compose up -d --build

# Ver logs
docker-compose logs -f backend

# Verificar
curl https://api.laesperanza.com/api/actuator/health
```

## 🆘 Troubleshooting

### Puerto 8080 ya en uso

```bash
# Encontrar proceso usando puerto
sudo lsof -i :8080

# Matar proceso
sudo kill -9 <PID>
```

### Memoria insuficiente

```bash
# Ver uso actual
docker stats

# Limitar en docker-compose.yml
environment:
  JAVA_OPTS: "-Xmx256m -Xms128m"
```

### BD corrupta

```bash
# Hacer backup primero
docker-compose exec postgres pg_dump -U postgres la_esperanza > backup.sql

# Reiniciar BD
docker-compose down
docker volume rm backend_postgres_data
docker-compose up -d
```

### SSL expirado

```bash
# Renovar manualmente
sudo certbot renew --force-renewal

# Verificar fechas
sudo certbot certificates
```

## ✅ Verificación Post-Despliegue

```bash
# 1. Verificar SSL
curl -I https://api.laesperanza.com/api/health

# 2. Verificar BD
curl -X GET https://api.laesperanza.com/api/categorias

# 3. Verificar autenticación
curl -X POST https://api.laesperanza.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"telefono":"50212345678","codigo":"1234"}'

# 4. Ver logs
docker-compose logs backend | tail -50

# 5. Ver métricas
curl https://api.laesperanza.com/api/actuator/health | jq .
```

## 📋 Checklist de Despliegue Completado

- [ ] Servidor configurado
- [ ] Docker instalado
- [ ] Repositorio clonado
- [ ] Variables de entorno configuradas
- [ ] SSL certificado obtenido
- [ ] Aplicación iniciada (docker-compose up)
- [ ] Nginx configurado
- [ ] Dominio apuntando correctamente
- [ ] Tests de salud pasando
- [ ] Backups configurados
- [ ] Monitoreo activo
- [ ] Documentación actualizada

## 📞 Soporte

- Documentación: [README.md](./README.md)
- Issues: [GitHub Issues](https://github.com/usuario/la-esperanza/issues)
- Email: soporte@laesperanza.com
