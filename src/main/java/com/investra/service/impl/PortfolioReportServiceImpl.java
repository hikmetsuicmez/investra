package com.investra.service.impl;

import com.investra.dtos.response.PortfolioReportResponse;
import com.investra.dtos.response.PortfolioSummaryReportResponse;
import com.investra.dtos.response.PortfolioPerformanceReportResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.*;
import com.investra.entity.TradeOrder;
import com.investra.enums.ClientStatus;
import com.investra.enums.ClientType;
import com.investra.enums.OrderStatus;
import com.investra.enums.SettlementStatus;
import com.investra.exception.ClientNotFoundException;
import com.investra.repository.*;
import com.investra.repository.TradeOrderRepository;
import com.investra.service.PortfolioReportService;
import com.investra.service.SimulationDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioReportServiceImpl implements PortfolioReportService {

    private final ClientRepository clientRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final StockDailyPriceRepository stockDailyPriceRepository;
    private final AccountRepository accountRepository;
    private final PortfolioRepository portfolioRepository;
    private final SimulationDateService simulationDateService;
    private final TradeOrderRepository tradeOrderRepository;

    @Override
    public Response<PortfolioReportResponse> getPortfolioReport(Long clientId, String date) {
        log.info("Portföy raporu oluşturuluyor. ClientId: {}, Date: {}", clientId, date);

        try {
            // Müşteri bilgilerini al
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new ClientNotFoundException("Müşteri bulunamadı: " + clientId));

            // Rapor tarihini parse et - günlük raporlar için simülasyon tarihini kullan
            LocalDate reportDate;
            if (date != null) {
                reportDate = LocalDate.parse(date);
            } else {
                // Tarih belirtilmemişse simülasyon tarihini kullan (günlük rapor)
                reportDate = simulationDateService.getCurrentSimulationDate();
                log.info("Günlük rapor için simülasyon tarihi kullanılıyor: {}", reportDate);
            }

            // Portföy bilgilerini al
            Portfolio portfolio = portfolioRepository.findByClient(client)
                    .orElseThrow(() -> new ClientNotFoundException("Müşteriye ait portföy bulunamadı: " + clientId));

            // Hesap bilgilerini al
            Account account = accountRepository.findByClientId(clientId)
                    .orElseThrow(() -> new ClientNotFoundException("Müşteriye ait hesap bulunamadı: " + clientId));

            // Settlement status'una göre işlemleri grupla
            Map<Long, PortfolioReportResponse.StockPositionDetail> stockPositionsMap = new HashMap<>();

            // 1. COMPLETED settlement'ları PortfolioItem'dan al (T+2 pozisyonları)
            List<PortfolioItem> portfolioItems = portfolioItemRepository.findByClientId(clientId);
            log.info("Müşteri {} için {} adet COMPLETED settlement (PortfolioItem) bulundu", clientId,
                    portfolioItems.size());

            for (PortfolioItem item : portfolioItems) {
                Long stockId = item.getStock().getId();

                if (!stockPositionsMap.containsKey(stockId)) {
                    stockPositionsMap.put(stockId, createEmptyStockPosition(item.getStock(), reportDate));
                }

                // COMPLETED settlement'ları T+2'ye ekle
                PortfolioReportResponse.StockPositionDetail currentPosition = stockPositionsMap.get(stockId);
                PortfolioReportResponse.StockPositionDetail updatedPosition = addPortfolioItemToPosition(
                        currentPosition, item, reportDate);
                stockPositionsMap.put(stockId, updatedPosition);
            }

            // 2. EXECUTED işlemleri al (PENDING, T1 settlement'ları)
            // T+2 settlement'lar zaten PortfolioItem'dan alındı, tekrar ekleme
            List<TradeOrder> executedTrades = tradeOrderRepository.findByClientAndStatus(client, OrderStatus.EXECUTED);
            log.info("Müşteri {} için toplam {} adet EXECUTED işlem bulundu", clientId, executedTrades.size());

            // Sadece PENDING ve T1 settlement'ları işle
            for (TradeOrder trade : executedTrades) {
                // COMPLETED ve T2 settlement'lar zaten PortfolioItem'dan alındı, tekrar ekleme
                if (trade.getSettlementStatus() == SettlementStatus.COMPLETED ||
                        trade.getSettlementStatus() == SettlementStatus.T2) {
                    continue;
                }

                Long stockId = trade.getStock().getId();

                if (!stockPositionsMap.containsKey(stockId)) {
                    stockPositionsMap.put(stockId, createEmptyStockPosition(trade.getStock(), reportDate));
                }

                // Settlement status'una göre miktarları ekle (PENDING=T+0, T1=T+1)
                PortfolioReportResponse.StockPositionDetail currentPosition = stockPositionsMap.get(stockId);
                PortfolioReportResponse.StockPositionDetail updatedPosition = addTradeToPosition(currentPosition, trade,
                        reportDate);
                stockPositionsMap.put(stockId, updatedPosition);
            }

            // Hisse senedi pozisyon detaylarını oluştur
            List<PortfolioReportResponse.StockPositionDetail> stockPositions = new ArrayList<>(
                    stockPositionsMap.values());

            // Toplam değerleri hesapla
            BigDecimal totalNominalValue = BigDecimal.ZERO;
            BigDecimal totalPotentialProfitLoss = BigDecimal.ZERO;
            BigDecimal totalPositionValue = BigDecimal.ZERO;

            for (PortfolioReportResponse.StockPositionDetail detail : stockPositions) {
                totalNominalValue = totalNominalValue.add(detail.getNominalValue());
                totalPotentialProfitLoss = totalPotentialProfitLoss.add(detail.getPotentialProfitLoss());
                totalPositionValue = totalPositionValue.add(detail.getNominalValue());
            }

            // Portföy değerlerini hesapla
            BigDecimal portfolioCurrentValue = totalPositionValue.add(account.getBalance());

            // Response'u oluştur
            PortfolioReportResponse report = PortfolioReportResponse.builder()
                    .customerName(client.getFullName())
                    .customerNumber(client.getTaxId())
                    .customerType(client.getClientType().name())
                    .accountNumber(account.getAccountNumber())
                    .reportDate(reportDate)
                    .portfolioCurrentValue(portfolioCurrentValue)
                    .totalPositionValue(totalPositionValue)
                    .tlBalance(account.getBalance())
                    .stockPositions(stockPositions)
                    .totalNominalValue(totalNominalValue)
                    .totalPotentialProfitLoss(totalPotentialProfitLoss)
                    .build();

            log.info("Portföy raporu başarıyla oluşturuldu. Müşteri: {}, Tarih: {}, Pozisyon sayısı: {}",
                    client.getFullName(), reportDate, stockPositions.size());

            return Response.<PortfolioReportResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .success(true)
                    .message("Portföy raporu başarıyla oluşturuldu")
                    .data(report)
                    .build();

        } catch (Exception e) {
            log.error("Portföy raporu oluşturulurken hata oluştu. ClientId: {}, Date: {}", clientId, date, e);
            return Response.<PortfolioReportResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .success(false)
                    .message("Portföy raporu oluşturulurken hata oluştu: " + e.getMessage())
                    .build();
        }
    }

    private PortfolioReportResponse.StockPositionDetail createEmptyStockPosition(Stock stock, LocalDate reportDate) {
        BigDecimal closingPrice = getClosingPrice(stock, reportDate);

        return PortfolioReportResponse.StockPositionDetail.builder()
                .stockCode(stock.getCode())
                .stockName(stock.getName())
                .t0Quantity(0)
                .t1Quantity(0)
                .t2Quantity(0)
                .buyPrice(BigDecimal.ZERO)
                .closingPrice(closingPrice)
                .nominalValue(BigDecimal.ZERO)
                .potentialProfitLoss(BigDecimal.ZERO)
                .profitLossRatio(BigDecimal.ZERO)
                .totalQuantity(0)
                .build();
    }

    private PortfolioReportResponse.StockPositionDetail addTradeToPosition(
            PortfolioReportResponse.StockPositionDetail currentPosition,
            TradeOrder trade,
            LocalDate reportDate) {

        Stock stock = trade.getStock();
        BigDecimal closingPrice = getClosingPrice(stock, reportDate);

        // Settlement status'una göre miktarları ayır
        int t0Quantity = currentPosition.getT0Quantity();
        int t1Quantity = currentPosition.getT1Quantity();
        int t2Quantity = currentPosition.getT2Quantity();

        switch (trade.getSettlementStatus()) {
            case PENDING:
                t0Quantity += trade.getQuantity();
                log.debug("PENDING settlement - Hisse: {}, Miktar: {}, T+0: {}",
                        stock.getCode(), trade.getQuantity(), t0Quantity);
                break;
            case T1:
                t1Quantity += trade.getQuantity();
                log.debug("T1 settlement - Hisse: {}, Miktar: {}, T+1: {}",
                        stock.getCode(), trade.getQuantity(), t1Quantity);
                break;
            case T2:
                // T+2 settlement'lar PortfolioItem'dan alınıyor, burada ekleme
                log.debug("T2 settlement atlandı (PortfolioItem'dan alınıyor) - Hisse: {}, Miktar: {}",
                        stock.getCode(), trade.getQuantity());
                break;
            case COMPLETED:
                // COMPLETED işlemler PortfolioItem'da zaten var, burada ekleme
                log.debug("COMPLETED settlement atlandı - Hisse: {}, Miktar: {}",
                        stock.getCode(), trade.getQuantity());
                break;
            default:
                // Diğer durumlar için T+0'a ekle
                t0Quantity += trade.getQuantity();
                log.debug("DEFAULT settlement - Hisse: {}, Miktar: {}, T+0: {}",
                        stock.getCode(), trade.getQuantity(), t0Quantity);
                break;
        }

        int totalQuantity = t0Quantity + t1Quantity + t2Quantity;

        // Ortalama alış fiyatını hesapla (sadece T+0 ve T+1 işlemler için)
        BigDecimal totalBuyPrice = calculateTotalBuyPrice(currentPosition, trade, trade.getSettlementStatus());
        BigDecimal avgBuyPrice = totalQuantity > 0
                ? totalBuyPrice.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Değerleri hesapla
        BigDecimal nominalValue = closingPrice.multiply(BigDecimal.valueOf(totalQuantity));
        BigDecimal potentialProfitLoss = closingPrice.subtract(avgBuyPrice)
                .multiply(BigDecimal.valueOf(totalQuantity));
        BigDecimal profitLossRatio = avgBuyPrice.compareTo(BigDecimal.ZERO) > 0 ? potentialProfitLoss
                .divide(avgBuyPrice.multiply(BigDecimal.valueOf(totalQuantity)), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        return PortfolioReportResponse.StockPositionDetail.builder()
                .stockCode(stock.getCode())
                .stockName(stock.getName())
                .t0Quantity(t0Quantity)
                .t1Quantity(t1Quantity)
                .t2Quantity(t2Quantity)
                .buyPrice(avgBuyPrice)
                .closingPrice(closingPrice)
                .nominalValue(nominalValue)
                .potentialProfitLoss(potentialProfitLoss)
                .profitLossRatio(profitLossRatio)
                .totalQuantity(totalQuantity)
                .build();
    }

    private PortfolioReportResponse.StockPositionDetail addPortfolioItemToPosition(
            PortfolioReportResponse.StockPositionDetail currentPosition,
            PortfolioItem item,
            LocalDate reportDate) {

        Stock stock = item.getStock();
        BigDecimal closingPrice = getClosingPrice(stock, reportDate);

        // PortfolioItem'lar COMPLETED settlement'ları temsil eder
        // Bunları T+2'ye ekle (zaten settled)
        int t2Quantity = currentPosition.getT2Quantity() + item.getQuantity();
        int totalQuantity = currentPosition.getT0Quantity() + currentPosition.getT1Quantity() + t2Quantity;

        // COMPLETED settlement'lar için ortalama alış fiyatını hesapla
        // PortfolioItem'ın avgPrice'ını kullan
        BigDecimal totalBuyPrice = currentPosition.getBuyPrice()
                .multiply(BigDecimal.valueOf(currentPosition.getTotalQuantity()))
                .add(item.getAvgPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        BigDecimal avgBuyPrice = totalQuantity > 0
                ? totalBuyPrice.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Değerleri hesapla
        BigDecimal nominalValue = closingPrice.multiply(BigDecimal.valueOf(totalQuantity));
        BigDecimal potentialProfitLoss = closingPrice.subtract(avgBuyPrice)
                .multiply(BigDecimal.valueOf(totalQuantity));
        BigDecimal profitLossRatio = avgBuyPrice.compareTo(BigDecimal.ZERO) > 0 ? potentialProfitLoss
                .divide(avgBuyPrice.multiply(BigDecimal.valueOf(totalQuantity)), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        log.debug("COMPLETED settlement ekleniyor - Hisse: {}, Miktar: {}, T+2: {}, Toplam: {}, Ortalama Fiyat: {}",
                stock.getCode(), item.getQuantity(), t2Quantity, totalQuantity, avgBuyPrice);

        return PortfolioReportResponse.StockPositionDetail.builder()
                .stockCode(stock.getCode())
                .stockName(stock.getName())
                .t0Quantity(currentPosition.getT0Quantity())
                .t1Quantity(currentPosition.getT1Quantity())
                .t2Quantity(t2Quantity)
                .buyPrice(avgBuyPrice)
                .closingPrice(closingPrice)
                .nominalValue(nominalValue)
                .potentialProfitLoss(potentialProfitLoss)
                .profitLossRatio(profitLossRatio)
                .totalQuantity(totalQuantity)
                .build();
    }

    private BigDecimal calculateTotalBuyPrice(
            PortfolioReportResponse.StockPositionDetail currentPosition,
            TradeOrder trade,
            SettlementStatus settlementStatus) {

        // Sadece T+0 ve T+1 işlemler için alış fiyatını hesapla
        // T+2 settlement'lar PortfolioItem'dan alınıyor
        BigDecimal currentTotalPrice = currentPosition.getBuyPrice()
                .multiply(BigDecimal.valueOf(currentPosition.getTotalQuantity()));

        if (settlementStatus == SettlementStatus.PENDING ||
                settlementStatus == SettlementStatus.T1) {
            return currentTotalPrice.add(trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity())));
        }

        return currentTotalPrice;
    }

    private BigDecimal getClosingPrice(Stock stock, LocalDate date) {
        StockDailyPrice price = stockDailyPriceRepository.findByStockIdAndPriceDate(stock.getId(), date);
        if (price != null && price.getClosePrice() != null) {
            return price.getClosePrice();
        }

        // Eğer o günün fiyatı yoksa, en son fiyatı al
        List<StockDailyPrice> prices = stockDailyPriceRepository.findAllByPriceDate(date);
        if (!prices.isEmpty()) {
            return prices.get(0).getClosePrice();
        }

        // Eğer hiç fiyat yoksa, stock'un price alanını kullan
        return stock.getPrice() != null ? stock.getPrice() : BigDecimal.ZERO;
    }

    @Override
    public byte[] exportPortfolioReportToExcel(Long clientId, String date) {
        try {
            Response<PortfolioReportResponse> reportResponse = getPortfolioReport(clientId, date);
            if (reportResponse.getData() == null) {
                throw new RuntimeException("Rapor verisi bulunamadı");
            }

            PortfolioReportResponse report = reportResponse.getData();

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Portföy Raporu");

                // Başlık satırı
                Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("Müşteri Adı ve Soyadı");
                headerRow.createCell(1).setCellValue("Müşteri No");
                headerRow.createCell(2).setCellValue("Müşteri Türü");
                headerRow.createCell(3).setCellValue("Hesap No");
                headerRow.createCell(4).setCellValue("Rapor Tarihi");
                headerRow.createCell(5).setCellValue("Portföy Güncel Değeri");
                headerRow.createCell(6).setCellValue("Toplam Pozisyon Değeri");
                headerRow.createCell(7).setCellValue("TL Bakiye");

                // Veri satırı
                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue(report.getCustomerName());
                dataRow.createCell(1).setCellValue(report.getCustomerNumber());
                dataRow.createCell(2).setCellValue(report.getCustomerType());
                dataRow.createCell(3).setCellValue(report.getAccountNumber());
                dataRow.createCell(4)
                        .setCellValue(report.getReportDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                dataRow.createCell(5).setCellValue(report.getPortfolioCurrentValue().doubleValue());
                dataRow.createCell(6).setCellValue(report.getTotalPositionValue().doubleValue());
                dataRow.createCell(7).setCellValue(report.getTlBalance().doubleValue());

                // Hisse senedi tablosu başlığı
                Row stockHeaderRow = sheet.createRow(3);
                stockHeaderRow.createCell(0).setCellValue("Hisselerim - Adetsel bazda");

                Row stockColumnsRow = sheet.createRow(4);
                stockColumnsRow.createCell(0).setCellValue("Hisse Kodu");
                stockColumnsRow.createCell(1).setCellValue("Hisse Adı");
                stockColumnsRow.createCell(2).setCellValue("T+0");
                stockColumnsRow.createCell(3).setCellValue("T+1");
                stockColumnsRow.createCell(4).setCellValue("T+2");
                stockColumnsRow.createCell(5).setCellValue("Alış Fiyatı");
                stockColumnsRow.createCell(6).setCellValue("Kapanış Fiyatı");
                stockColumnsRow.createCell(7).setCellValue("Nominal Değeri");
                stockColumnsRow.createCell(8).setCellValue("Potansiyel Kar/Zarar");
                stockColumnsRow.createCell(9).setCellValue("Kar/Zarar Oranı (%)");

                // Hisse senedi verileri
                int rowNum = 5;
                for (PortfolioReportResponse.StockPositionDetail stock : report.getStockPositions()) {
                    Row stockRow = sheet.createRow(rowNum++);
                    stockRow.createCell(0).setCellValue(stock.getStockCode());
                    stockRow.createCell(1).setCellValue(stock.getStockName());
                    stockRow.createCell(2).setCellValue(stock.getT0Quantity());
                    stockRow.createCell(3).setCellValue(stock.getT1Quantity());
                    stockRow.createCell(4).setCellValue(stock.getT2Quantity());
                    stockRow.createCell(5).setCellValue(stock.getBuyPrice().doubleValue());
                    stockRow.createCell(6).setCellValue(stock.getClosingPrice().doubleValue());
                    stockRow.createCell(7).setCellValue(stock.getNominalValue().doubleValue());
                    stockRow.createCell(8).setCellValue(stock.getPotentialProfitLoss().doubleValue());
                    stockRow.createCell(9).setCellValue(stock.getProfitLossRatio().doubleValue());
                }

                // Toplam satırı
                Row totalRow = sheet.createRow(rowNum + 1);
                totalRow.createCell(0).setCellValue("TOPLAM");
                totalRow.createCell(7).setCellValue(report.getTotalNominalValue().doubleValue());
                totalRow.createCell(8).setCellValue(report.getTotalPotentialProfitLoss().doubleValue());

                // Sütun genişliklerini ayarla
                for (int i = 0; i < 10; i++) {
                    sheet.autoSizeColumn(i);
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }

        } catch (Exception e) {
            log.error("Excel export hatası: {}", e.getMessage(), e);
            throw new RuntimeException("Excel export hatası: " + e.getMessage());
        }
    }

    @Override
    public byte[] exportPortfolioReportToPdf(Long clientId, String date) {
        // PDF export şimdilik devre dışı - iText dependency sorunu
        throw new UnsupportedOperationException("PDF export şimdilik desteklenmiyor");
    }

    @Override
    public Response<List<PortfolioReportResponse>> getAllPortfolioReports(String date) {
        log.info("Tüm portföy raporları oluşturuluyor. Date: {}", date);

        try {
            LocalDate reportDate;
            if (date != null) {
                reportDate = LocalDate.parse(date);
            } else {
                // Tarih belirtilmemişse simülasyon tarihini kullan (günlük rapor)
                reportDate = simulationDateService.getCurrentSimulationDate();
                log.info("Günlük rapor için simülasyon tarihi kullanılıyor: {}", reportDate);
            }

            // Tüm aktif müşterileri al
            List<Client> activeClients = clientRepository.findByStatus(ClientStatus.ACTIVE);

            List<PortfolioReportResponse> reports = new ArrayList<>();

            for (Client client : activeClients) {
                try {
                    Response<PortfolioReportResponse> reportResponse = getPortfolioReport(client.getId(), date);
                    if (reportResponse.getData() != null) {
                        reports.add(reportResponse.getData());
                    }
                } catch (Exception e) {
                    log.warn("Müşteri {} için rapor oluşturulamadı: {}", client.getId(), e.getMessage());
                }
            }

            return Response.<List<PortfolioReportResponse>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Tüm portföy raporları başarıyla oluşturuldu")
                    .data(reports)
                    .build();

        } catch (Exception e) {
            log.error("Tüm portföy raporları oluşturulurken hata: {}", e.getMessage(), e);
            return Response.<List<PortfolioReportResponse>>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Tüm portföy raporları oluşturulamadı: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response<List<PortfolioReportResponse>> getPortfolioReportsByDate(String date) {
        log.info("Tarihe göre portföy raporları oluşturuluyor. Date: {}", date);

        try {
            LocalDate reportDate = LocalDate.parse(date);
            return getAllPortfolioReports(date);

        } catch (Exception e) {
            log.error("Tarihe göre portföy raporları oluşturulurken hata: {}", e.getMessage(), e);
            return Response.<List<PortfolioReportResponse>>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Tarihe göre portföy raporları oluşturulamadı: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response<List<PortfolioReportResponse>> getPortfolioReportsByClientType(String clientType) {
        log.info("Müşteri tipine göre portföy raporları oluşturuluyor. ClientType: {}", clientType);

        try {
            // Müşteri tipini parse et
            ClientType type = ClientType.valueOf(clientType.toUpperCase());

            // O tipteki aktif müşterileri al
            List<Client> clients = clientRepository.findByClientTypeAndStatus(type, ClientStatus.ACTIVE);

            List<PortfolioReportResponse> reports = new ArrayList<>();

            for (Client client : clients) {
                try {
                    Response<PortfolioReportResponse> reportResponse = getPortfolioReport(client.getId(), null);
                    if (reportResponse.getData() != null) {
                        reports.add(reportResponse.getData());
                    }
                } catch (Exception e) {
                    log.warn("Müşteri {} için rapor oluşturulamadı: {}", client.getId(), e.getMessage());
                }
            }

            return Response.<List<PortfolioReportResponse>>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Müşteri tipine göre portföy raporları başarıyla oluşturuldu")
                    .data(reports)
                    .build();

        } catch (Exception e) {
            log.error("Müşteri tipine göre portföy raporları oluşturulurken hata: {}", e.getMessage(), e);
            return Response.<List<PortfolioReportResponse>>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Müşteri tipine göre portföy raporları oluşturulamadı: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response<PortfolioSummaryReportResponse> getPortfolioSummaryReport(String date) {
        log.info("Portföy özet raporu oluşturuluyor. Date: {}", date);

        try {
            LocalDate reportDate;
            if (date != null) {
                reportDate = LocalDate.parse(date);
            } else {
                // Tarih belirtilmemişse simülasyon tarihini kullan (günlük rapor)
                reportDate = simulationDateService.getCurrentSimulationDate();
                log.info("Günlük rapor için simülasyon tarihi kullanılıyor: {}", reportDate);
            }

            // Tüm aktif müşterileri al
            List<Client> activeClients = clientRepository.findByStatus(ClientStatus.ACTIVE);

            BigDecimal totalPortfolioValue = BigDecimal.ZERO;
            BigDecimal totalCashBalance = BigDecimal.ZERO;
            BigDecimal totalStockValue = BigDecimal.ZERO;

            // Müşteri tipine göre dağılım
            List<PortfolioSummaryReportResponse.ClientTypeSummary> clientTypeDistribution = new ArrayList<>();

            // En büyük portföyler
            List<PortfolioSummaryReportResponse.TopPortfolio> topPortfolios = new ArrayList<>();

            // En çok işlem yapılan hisseler
            List<PortfolioSummaryReportResponse.TopStock> topStocks = new ArrayList<>();

            for (Client client : activeClients) {
                try {
                    Response<PortfolioReportResponse> reportResponse = getPortfolioReport(client.getId(), date);
                    if (reportResponse.getData() != null) {
                        PortfolioReportResponse report = reportResponse.getData();

                        totalPortfolioValue = totalPortfolioValue.add(report.getPortfolioCurrentValue());
                        totalCashBalance = totalCashBalance.add(report.getTlBalance());
                        totalStockValue = totalStockValue.add(report.getTotalPositionValue());

                        // Top portföyler listesine ekle
                        topPortfolios.add(PortfolioSummaryReportResponse.TopPortfolio.builder()
                                .clientId(client.getId())
                                .clientName(client.getFullName())
                                .clientType(client.getClientType().name())
                                .portfolioValue(report.getPortfolioCurrentValue())
                                .cashBalance(report.getTlBalance())
                                .stockValue(report.getTotalPositionValue())
                                .build());
                    }
                } catch (Exception e) {
                    log.warn("Müşteri {} için rapor oluşturulamadı: {}", client.getId(), e.getMessage());
                }
            }

            // Top portföyleri değere göre sırala
            topPortfolios.sort((a, b) -> b.getPortfolioValue().compareTo(a.getPortfolioValue()));
            if (topPortfolios.size() > 10) {
                topPortfolios = topPortfolios.subList(0, 10);
            }

            // Müşteri tipine göre dağılımı hesapla
            Map<ClientType, List<Client>> clientsByType = activeClients.stream()
                    .collect(Collectors.groupingBy(Client::getClientType));

            for (Map.Entry<ClientType, List<Client>> entry : clientsByType.entrySet()) {
                ClientType type = entry.getKey();
                List<Client> clients = entry.getValue();

                BigDecimal typeTotalValue = BigDecimal.ZERO;
                for (Client client : clients) {
                    try {
                        Response<PortfolioReportResponse> reportResponse = getPortfolioReport(client.getId(), date);
                        if (reportResponse.getData() != null) {
                            typeTotalValue = typeTotalValue.add(reportResponse.getData().getPortfolioCurrentValue());
                        }
                    } catch (Exception e) {
                        // Hata durumunda devam et
                    }
                }

                BigDecimal averageValue = clients.size() > 0
                        ? typeTotalValue.divide(BigDecimal.valueOf(clients.size()), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                clientTypeDistribution.add(PortfolioSummaryReportResponse.ClientTypeSummary.builder()
                        .clientType(type.name())
                        .clientCount(clients.size())
                        .totalValue(typeTotalValue)
                        .averageValue(averageValue)
                        .build());
            }

            // En çok işlem yapılan hisseleri hesapla
            List<PortfolioItem> allPortfolioItems = portfolioItemRepository.findAll();
            Map<Stock, List<PortfolioItem>> itemsByStock = allPortfolioItems.stream()
                    .collect(Collectors.groupingBy(PortfolioItem::getStock));

            for (Map.Entry<Stock, List<PortfolioItem>> entry : itemsByStock.entrySet()) {
                Stock stock = entry.getKey();
                List<PortfolioItem> items = entry.getValue();

                Integer totalQuantity = items.stream()
                        .mapToInt(PortfolioItem::getQuantity)
                        .sum();

                BigDecimal totalValue = getClosingPrice(stock, reportDate)
                        .multiply(BigDecimal.valueOf(totalQuantity));

                Integer clientCount = items.stream()
                        .map(item -> item.getAccount().getClient().getId())
                        .distinct()
                        .collect(Collectors.toList())
                        .size();

                topStocks.add(PortfolioSummaryReportResponse.TopStock.builder()
                        .stockCode(stock.getCode())
                        .stockName(stock.getName())
                        .totalQuantity(totalQuantity)
                        .totalValue(totalValue)
                        .clientCount(clientCount)
                        .build());
            }

            // Top hisseleri değere göre sırala
            topStocks.sort((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()));
            if (topStocks.size() > 10) {
                topStocks = topStocks.subList(0, 10);
            }

            PortfolioSummaryReportResponse summaryReport = PortfolioSummaryReportResponse.builder()
                    .reportDate(reportDate)
                    .totalClients(activeClients.size())
                    .totalPortfolioValue(totalPortfolioValue)
                    .totalCashBalance(totalCashBalance)
                    .totalStockValue(totalStockValue)
                    .clientTypeDistribution(clientTypeDistribution)
                    .topPortfolios(topPortfolios)
                    .topStocks(topStocks)
                    .build();

            return Response.<PortfolioSummaryReportResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Portföy özet raporu başarıyla oluşturuldu")
                    .data(summaryReport)
                    .build();

        } catch (Exception e) {
            log.error("Portföy özet raporu oluşturulurken hata: {}", e.getMessage(), e);
            return Response.<PortfolioSummaryReportResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Portföy özet raporu oluşturulamadı: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response<PortfolioPerformanceReportResponse> getPortfolioPerformanceReport(String startDate,
            String endDate) {
        log.info("Portföy performans raporu oluşturuluyor. StartDate: {}, EndDate: {}", startDate, endDate);

        try {
            LocalDate start;
            LocalDate end;

            if (startDate != null) {
                start = LocalDate.parse(startDate);
            } else {
                // Başlangıç tarihi belirtilmemişse simülasyon tarihinden 30 gün öncesini kullan
                start = simulationDateService.getCurrentSimulationDate().minusDays(30);
                log.info("Performans raporu için başlangıç tarihi hesaplandı: {} (simülasyon tarihinden 30 gün önce)",
                        start);
            }

            if (endDate != null) {
                end = LocalDate.parse(endDate);
            } else {
                // Bitiş tarihi belirtilmemişse simülasyon tarihini kullan
                end = simulationDateService.getCurrentSimulationDate();
                log.info("Performans raporu için bitiş tarihi: {} (simülasyon tarihi)", end);
            }

            // Tüm aktif müşterileri al
            List<Client> activeClients = clientRepository.findByStatus(ClientStatus.ACTIVE);

            List<PortfolioPerformanceReportResponse.BestPerformingPortfolio> bestPerformingPortfolios = new ArrayList<>();
            List<PortfolioPerformanceReportResponse.WorstPerformingPortfolio> worstPerformingPortfolios = new ArrayList<>();
            List<PortfolioPerformanceReportResponse.DailyPerformance> dailyPerformances = new ArrayList<>();

            // Günlük performans hesapla
            LocalDate currentDate = start;
            while (!currentDate.isAfter(end)) {
                BigDecimal dailyTotalValue = BigDecimal.ZERO;
                Integer activeClientsCount = 0;

                for (Client client : activeClients) {
                    try {
                        Response<PortfolioReportResponse> reportResponse = getPortfolioReport(client.getId(),
                                currentDate.toString());
                        if (reportResponse.getData() != null) {
                            dailyTotalValue = dailyTotalValue.add(reportResponse.getData().getPortfolioCurrentValue());
                            activeClientsCount++;
                        }
                    } catch (Exception e) {
                        // Hata durumunda devam et
                    }
                }

                dailyPerformances.add(PortfolioPerformanceReportResponse.DailyPerformance.builder()
                        .date(currentDate)
                        .totalPortfolioValue(dailyTotalValue)
                        .activeClients(activeClientsCount)
                        .build());

                currentDate = currentDate.plusDays(1);
            }

            // Günlük değişimleri hesapla
            for (int i = 1; i < dailyPerformances.size(); i++) {
                PortfolioPerformanceReportResponse.DailyPerformance current = dailyPerformances.get(i);
                PortfolioPerformanceReportResponse.DailyPerformance previous = dailyPerformances.get(i - 1);

                BigDecimal changeValue = current.getTotalPortfolioValue().subtract(previous.getTotalPortfolioValue());
                BigDecimal changePercentage = previous.getTotalPortfolioValue().compareTo(BigDecimal.ZERO) > 0
                        ? changeValue.divide(previous.getTotalPortfolioValue(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO;

                current.setChangeValue(changeValue);
                current.setChangePercentage(changePercentage);
            }

            // En iyi ve en kötü performans gösteren portföyleri bul
            for (Client client : activeClients) {
                try {
                    Response<PortfolioReportResponse> startReport = getPortfolioReport(client.getId(), startDate);
                    Response<PortfolioReportResponse> endReport = getPortfolioReport(client.getId(), endDate);

                    if (startReport.getData() != null && endReport.getData() != null) {
                        PortfolioReportResponse startData = startReport.getData();
                        PortfolioReportResponse endData = endReport.getData();

                        BigDecimal changeValue = endData.getPortfolioCurrentValue()
                                .subtract(startData.getPortfolioCurrentValue());
                        BigDecimal changePercentage = startData.getPortfolioCurrentValue()
                                .compareTo(BigDecimal.ZERO) > 0
                                        ? changeValue
                                                .divide(startData.getPortfolioCurrentValue(), 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                        : BigDecimal.ZERO;

                        PortfolioPerformanceReportResponse.BestPerformingPortfolio bestPortfolio = PortfolioPerformanceReportResponse.BestPerformingPortfolio
                                .builder()
                                .clientId(client.getId())
                                .clientName(client.getFullName())
                                .clientType(client.getClientType().name())
                                .startValue(startData.getPortfolioCurrentValue())
                                .endValue(endData.getPortfolioCurrentValue())
                                .changeValue(changeValue)
                                .changePercentage(changePercentage)
                                .build();

                        PortfolioPerformanceReportResponse.WorstPerformingPortfolio worstPortfolio = PortfolioPerformanceReportResponse.WorstPerformingPortfolio
                                .builder()
                                .clientId(client.getId())
                                .clientName(client.getFullName())
                                .clientType(client.getClientType().name())
                                .startValue(startData.getPortfolioCurrentValue())
                                .endValue(endData.getPortfolioCurrentValue())
                                .changeValue(changeValue)
                                .changePercentage(changePercentage)
                                .build();

                        bestPerformingPortfolios.add(bestPortfolio);
                        worstPerformingPortfolios.add(worstPortfolio);
                    }
                } catch (Exception e) {
                    log.warn("Müşteri {} için performans raporu oluşturulamadı: {}", client.getId(), e.getMessage());
                }
            }

            // En iyi ve en kötü performans gösteren portföyleri sırala
            bestPerformingPortfolios.sort((a, b) -> b.getChangePercentage().compareTo(a.getChangePercentage()));
            worstPerformingPortfolios.sort((a, b) -> a.getChangePercentage().compareTo(b.getChangePercentage()));

            if (bestPerformingPortfolios.size() > 10) {
                bestPerformingPortfolios = bestPerformingPortfolios.subList(0, 10);
            }
            if (worstPerformingPortfolios.size() > 10) {
                worstPerformingPortfolios = worstPerformingPortfolios.subList(0, 10);
            }

            // Hisse senedi bazında performans hesapla
            List<PortfolioPerformanceReportResponse.StockPerformance> stockPerformances = new ArrayList<>();
            List<PortfolioItem> allPortfolioItems = portfolioItemRepository.findAll();
            Map<Stock, List<PortfolioItem>> itemsByStock = allPortfolioItems.stream()
                    .collect(Collectors.groupingBy(PortfolioItem::getStock));

            for (Map.Entry<Stock, List<PortfolioItem>> entry : itemsByStock.entrySet()) {
                Stock stock = entry.getKey();
                List<PortfolioItem> items = entry.getValue();

                BigDecimal startPrice = getClosingPrice(stock, start);
                BigDecimal endPrice = getClosingPrice(stock, end);
                BigDecimal changePrice = endPrice.subtract(startPrice);
                BigDecimal changePercentage = startPrice.compareTo(BigDecimal.ZERO) > 0
                        ? changePrice.divide(startPrice, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                        : BigDecimal.ZERO;

                Integer totalQuantity = items.stream()
                        .mapToInt(PortfolioItem::getQuantity)
                        .sum();

                BigDecimal totalValue = endPrice.multiply(BigDecimal.valueOf(totalQuantity));

                stockPerformances.add(PortfolioPerformanceReportResponse.StockPerformance.builder()
                        .stockCode(stock.getCode())
                        .stockName(stock.getName())
                        .startPrice(startPrice)
                        .endPrice(endPrice)
                        .changePrice(changePrice)
                        .changePercentage(changePercentage)
                        .totalQuantity(totalQuantity)
                        .totalValue(totalValue)
                        .build());
            }

            // Genel performans metriklerini hesapla
            BigDecimal totalPortfolioValueChange = dailyPerformances.get(dailyPerformances.size() - 1)
                    .getTotalPortfolioValue()
                    .subtract(dailyPerformances.get(0).getTotalPortfolioValue());

            BigDecimal totalPortfolioValueChangePercentage = dailyPerformances.get(0).getTotalPortfolioValue()
                    .compareTo(BigDecimal.ZERO) > 0
                            ? totalPortfolioValueChange
                                    .divide(dailyPerformances.get(0).getTotalPortfolioValue(), 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;

            BigDecimal averagePortfolioValueChange = totalPortfolioValueChange
                    .divide(BigDecimal.valueOf(activeClients.size()), 2, RoundingMode.HALF_UP);
            BigDecimal averagePortfolioValueChangePercentage = totalPortfolioValueChangePercentage
                    .divide(BigDecimal.valueOf(activeClients.size()), 2, RoundingMode.HALF_UP);

            PortfolioPerformanceReportResponse performanceReport = PortfolioPerformanceReportResponse.builder()
                    .startDate(start)
                    .endDate(end)
                    .totalClients(activeClients.size())
                    .totalPortfolioValueChange(totalPortfolioValueChange)
                    .totalPortfolioValueChangePercentage(totalPortfolioValueChangePercentage)
                    .averagePortfolioValueChange(averagePortfolioValueChange)
                    .averagePortfolioValueChangePercentage(averagePortfolioValueChangePercentage)
                    .bestPerformingPortfolios(bestPerformingPortfolios)
                    .worstPerformingPortfolios(worstPerformingPortfolios)
                    .stockPerformances(stockPerformances)
                    .dailyPerformances(dailyPerformances)
                    .build();

            return Response.<PortfolioPerformanceReportResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Portföy performans raporu başarıyla oluşturuldu")
                    .data(performanceReport)
                    .build();

        } catch (Exception e) {
            log.error("Portföy performans raporu oluşturulurken hata: {}", e.getMessage(), e);
            return Response.<PortfolioPerformanceReportResponse>builder()
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Portföy performans raporu oluşturulamadı: " + e.getMessage())
                    .build();
        }
    }
}
