# Investra DevOps Rehberi

## 🚀 DevOps Stack

Investra uygulaması aşağıdaki DevOps bileşenlerini içerir:

- **Containerization**: Docker & Docker Compose
- **Cache**: Redis
- **Monitoring**: Prometheus & Grafana
- **Health Checks**: Actuator & Custom Scripts
- **Logging**: Application & Container logs

## 🛠️ Kurulum

### Ön Gereksinimler

- Docker ve Docker Compose
- JDK 17
- Maven

### Environment Variables

`.env` dosyası oluşturun (`.env.example` dosyasını referans alabilirsiniz):

```env
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_email_password
FRONTEND_URL=http://localhost:3000
INFINA_API_KEY=your_api_key
GRAFANA_PASSWORD=admin123
```

### Başlatma

```bash
# Build ve start
docker compose up -d --build

# Logları izleme
docker compose logs -f

# Health check
./health-check.sh
```

## 📊 Monitoring

### Prometheus (http://localhost:9090)
- Metrikler 15 saniyelik aralıklarla toplanır
- Spring Boot Actuator ve Redis metrikleri
- 200 saatlik veri retention

### Grafana (http://localhost:3000)
- Default credentials: admin/admin123
- Pre-configured dashboards:
  - JVM Metrics
  - Redis Metrics
  - Application Metrics

## 🔄 Cache (Redis)

Redis cache aşağıdaki veriler için kullanılmaktadır:

- **Stock Cache**: 
  - `stocks`: Hisse senedi listesi (10 dakika TTL)
  - `stock_{code}`: Tekil hisse bilgisi (10 dakika TTL)

- **Account Cache**:
  - `accounts`: Hesap detayları (10 dakika TTL)
  - `client-accounts`: Müşteri hesap listesi (10 dakika TTL)
  - `recent-clients`: Son eklenen müşteriler (10 dakika TTL)

## 🔍 Health Checks

- **Application**: `/actuator/health`
- **Redis**: Redis ping check
- **Database**: Connection pool status
- **Custom Script**: `health-check.sh`

## 📦 Docker Services

```yaml
services:
  app:         # Spring Boot Application (8088, 8089)
  redis:       # Cache Service (6379)
  prometheus:  # Metrics Collection (9090)
  grafana:     # Metrics Visualization (3000)
```

## 🔧 Production Ayarları

Production ortamı için özelleştirilmiş konfigürasyonlar:

- **Database**: Connection pool optimization
- **Hibernate**: No DDL auto update
- **Logging**: File-based logging
- **Cache**: Redis persistence
- **Security**: Restricted Actuator access

## 📝 Logging

- **Application Logs**: `/app/logs/investra.log`
- **Container Logs**: `docker compose logs`
- **Redis Logs**: Redis container logs
- **Prometheus Logs**: Prometheus container logs

## 🚨 Troubleshooting

1. **Container Başlatma Sorunları**
   ```bash
   # Tüm containerları yeniden başlat
   docker compose down
   docker compose up -d
   ```

2. **Cache Sorunları**
   ```bash
   # Redis CLI'a bağlan
   docker compose exec redis redis-cli
   # Cache'i temizle
   FLUSHALL
   ```

3. **Health Check Sorunları**
   ```bash
   # Health check script'ini çalıştır
   ./health-check.sh
   # Actuator endpoint'ini kontrol et
   curl http://localhost:8089/actuator/health
   ```

## 🔐 Security

- Actuator endpoints sadece authorized users tarafından erişilebilir
- Redis şifre korumalı (production'da aktif edilmeli)
- Grafana admin şifresi environment variable ile ayarlanır

## 📈 Scaling

Horizontal scaling için öneriler:

1. Redis Cluster kurulumu
2. Load Balancer eklenmesi
3. Multiple application instances

## 🤝 Contributing

1. Feature branch oluşturun
2. Değişikliklerinizi commit edin
3. Pull request açın

## 📚 Referanslar

- [Docker Documentation](https://docs.docker.com/)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Redis Documentation](https://redis.io/documentation)
- [Prometheus Documentation](https://prometheus.io/docs/introduction/overview/)
- [Grafana Documentation](https://grafana.com/docs/)
