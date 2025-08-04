#!/bin/bash

# Investra Application Health Check Script
# Bu script Docker container'ının sağlıklı çalışıp çalışmadığını kontrol eder

set -e

# Renk kodları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "🔍 Investra Health Check başlatılıyor..."

# 1. Application Health Check
echo "📱 Application health kontrol ediliyor..."
if curl -f -s http://localhost:8089/actuator/health > /dev/null; then
    echo -e "${GREEN}✅ Application sağlıklı${NC}"
else
    echo -e "${RED}❌ Application sağlıksız${NC}"
    exit 1
fi

# 2. Redis Connection Check
echo "🔴 Redis bağlantısı kontrol ediliyor..."
if curl -f -s http://localhost:8089/actuator/health | grep -q "redis.*UP"; then
    echo -e "${GREEN}✅ Redis bağlantısı aktif${NC}"
else
    echo -e "${YELLOW}⚠️  Redis bağlantısı kontrol edilemiyor${NC}"
fi

# 3. Database Connection Check
echo "🗄️  Database bağlantısı kontrol ediliyor..."
if curl -f -s http://localhost:8089/actuator/health | grep -q "db.*UP"; then
    echo -e "${GREEN}✅ Database bağlantısı aktif${NC}"
else
    echo -e "${YELLOW}⚠️  Database bağlantısı kontrol edilemiyor${NC}"
fi

# 4. API Endpoint Test
echo "🌐 API endpoint testi..."
if curl -f -s http://localhost:8088/api/accounts/recent-clients?limit=1 > /dev/null; then
    echo -e "${GREEN}✅ API endpoint'leri çalışıyor${NC}"
else
    echo -e "${RED}❌ API endpoint'leri yanıt vermiyor${NC}"
    exit 1
fi

echo -e "${GREEN}🎉 Tüm health check'ler başarılı!${NC}"
exit 0
