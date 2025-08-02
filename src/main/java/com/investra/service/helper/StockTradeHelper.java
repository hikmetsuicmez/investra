package com.investra.service.helper;

import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.PortfolioItem;
import com.investra.entity.Stock;
import com.investra.enums.ExecutionType;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.PortfolioItemRepository;
import com.investra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class StockTradeHelper {

    // Sabit oranlar
    private static final BigDecimal INDIVIDUAL_COMMISSION_RATE = new BigDecimal("0.002");  // Bireysel müşteriler için %0.2
    private static final BigDecimal CORPORATE_COMMISSION_RATE = new BigDecimal("0.001");   // Kurumsal müşteriler için %0.1
    private static final BigDecimal BSMV_RATE = new BigDecimal("0.05");                   // BSMV %5

    private final ClientRepository clientRepository;
    private final StockRepository stockRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final AccountRepository accountRepository;

    /**
     * Satış isteğinin geçerliliğini kontrol eder
     */
    public void validateSellOrderRequest(StockSellOrderRequest request) {
        if (request.getClientId() == null) {
            throw new RuntimeException("Müşteri kimliği boş olamaz");
        }

        if (request.getStockId() == null) {
            throw new RuntimeException("Hisse senedi kimliği boş olamaz");
        }

        if (request.getQuantity() <= 0) {
            throw new RuntimeException("Satış miktarı sıfırdan büyük olmalıdır");
        }

        if (request.getExecutionType() == ExecutionType.LIMIT && (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
            throw new RuntimeException("Limit emir için geçerli bir fiyat belirtilmelidir");
        }
    }

    /**
     * İstek için gerekli varlıkları bulur ve döndürür
     */
    public TradeEntities findTradeEntities(StockSellOrderRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));

        Stock stock = stockRepository.findById(request.getStockId())
                .orElseThrow(() -> new RuntimeException("Hisse senedi bulunamadı"));

        PortfolioItem portfolioItem = portfolioItemRepository
                .findByClientIdAndStockId(request.getClientId(), request.getStockId())
                .orElseThrow(() -> new RuntimeException("Müşterinin portföyünde bu hisse senedi bulunmamaktadır"));

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı"));

        // Miktar kontrolü
        if (portfolioItem.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Yeterli miktarda hisse senedi bulunmamaktadır");
        }

        return new TradeEntities(client, stock, portfolioItem, account);
    }

    /**
     * İşlem fiyatını hesaplar
     */
    public BigDecimal calculatePrice(StockSellOrderRequest request, Stock stock) {
        return request.getExecutionType() == ExecutionType.MARKET ?
                stock.getCurrentPrice() : request.getPrice();
    }

    /**
     * Komisyon oranını hesaplar
     */
    public BigDecimal getCommissionRate(Client client) {
        return "KURUMSAL".equals(client.getClientType()) ?
                CORPORATE_COMMISSION_RATE : INDIVIDUAL_COMMISSION_RATE;
    }

    /**
     * İşlem için finansal hesaplamaları yapar
     */
    public TradeCalculation calculateTradeAmounts(BigDecimal price, int quantity, Client client) {
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));

        BigDecimal commissionRate = getCommissionRate(client);
        BigDecimal commission = totalAmount.multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal bsmv = commission.multiply(BSMV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalTaxAndCommission = commission.add(bsmv).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netAmount = totalAmount.subtract(totalTaxAndCommission).setScale(2, RoundingMode.HALF_UP);

        return new TradeCalculation(totalAmount, commission, bsmv, totalTaxAndCommission, netAmount);
    }

    /**
     * Değer tarihini hesaplar (T+2)
     */
    public String calculateValueDate() {
        return "T+2";
    }

    /**
     * İşlem tarihini döndürür
     */
    public LocalDate getTradeDate() {
        return LocalDate.now();
    }

    /**
     * İşlem için gerekli varlıkları tutan yardımcı sınıf
     */
    public record TradeEntities(Client client, Stock stock, PortfolioItem portfolioItem, Account account) {}

    /**
     * İşlem için finansal hesaplamaları tutan yardımcı sınıf
     */
    public record TradeCalculation(
            BigDecimal totalAmount,
            BigDecimal commission,
            BigDecimal bsmv,
            BigDecimal totalTaxAndCommission,
            BigDecimal netAmount) {}
}
