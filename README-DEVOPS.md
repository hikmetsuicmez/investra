# Investra DevOps

Bu döküman, Investra uygulamasının Redis cache, Docker ve monitoring entegrasyonları için kurulum talimatlarını içerir.

## Geliştirme Ortamı Kurulumu

### Redis Cache Entegrasyonu İçin:

#### Seçenek 1: Redis'i Docker ile çalıştırma (Önerilen)
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

#### Seçenek 2: Redis'i yerel olarak kurma
- Windows: [Redis Windows](https://github.com/microsoftarchive/redis/releases) adresinden indirip kurabilirsiniz
- Mac: `brew install redis && brew services start redis`
- Linux: `sudo apt-get install redis-server && sudo systemctl start redis-server`

### Monitoring İçin:
Prometheus ve Grafana'yı Docker ile çalıştırabilirsiniz:
```bash
docker-compose up -d prometheus grafana
```

## Tüm Uygulamayı Docker ile Çalıştırma
Tüm ortamı (Uygulama, Redis, Prometheus, Grafana) tek seferde başlatmak için:
```bash
docker-compose up -d
```

## Servislere Erişim:
- Uygulama: http://localhost:8088
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (kullanıcı adı/şifre: admin/admin)

## Cache Kullanımı
Örnek olarak hisse senetleri için oluşturulan servisler Redis cache ile çalışmaktadır:
- Tüm hisseler: `/api/stocks`
- ID'ye göre hisse: `/api/stocks/{id}`
- Sembole göre hisse: `/api/stocks/symbol/{symbol}`

Projede cache kullanmak için `@Cacheable`, `@CacheEvict` veya `@CachePut` annotasyonlarını kullanabilirsiniz.

Örnek kullanım:
```java
@Cacheable(value = "entityName", key = "#id")
public Entity findById(Long id) {
    // DB sorgusu
}
```
