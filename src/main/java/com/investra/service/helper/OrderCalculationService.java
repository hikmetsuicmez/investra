package com.investra.service.helper;

import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.StockSellOrderPreviewResponse;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Stock;
import com.investra.enums.ClientType;
import com.investra.enums.ExecutionType;
import com.investra.exception.CalculationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
@Slf4j
public class OrderCalculationService {

    private static final BigDecimal INDIVIDUAL_COMMISION_RATE = new BigDecimal("0.002"); // %0.2
    private static final BigDecimal CORPORATE_COMMISION_RATE = new BigDecimal("0.001"); // %0.1
    private static final BigDecimal BSMV_FEE_RATE = new BigDecimal("0.05");  // %5
    private static final int DECIMAL_SCALE = 2;

    public OrderCalculation calculateOrderAmounts(Client client, Stock stock, StockSellOrderRequest request) {
        try {
            if (client == null || stock == null) {
                throw new IllegalArgumentException("Müşteri veya hisse senedi bilgisi boş olamaz");
            }

            BigDecimal commissionRate = client.getClientType() == ClientType.INDIVIDUAL
                    ? INDIVIDUAL_COMMISION_RATE
                    : CORPORATE_COMMISION_RATE;

            BigDecimal price = request.getExecutionType() == ExecutionType.MARKET
                    ? stock.getCurrentPrice()
                    : request.getPrice();

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Geçersiz fiyat: " + price);
            }
            BigDecimal quantity = BigDecimal.valueOf(request.getQuantity());

            BigDecimal commission = price
                    .multiply(quantity)
                    .multiply(commissionRate)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal bsmv = commission
                    .multiply(BSMV_FEE_RATE)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal totalTaxAndCommission = commission.add(bsmv)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal totalAmount = price
                    .multiply(quantity)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal netAmount = totalAmount
                    .subtract(totalTaxAndCommission)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            return new OrderCalculation(
                    price,
                    commission,
                    bsmv,
                    totalTaxAndCommission,
                    totalAmount,
                    netAmount
            );
        } catch (ArithmeticException e) {
            log.error("Hesaplama sırasında aritmetik hata oluştu: {}", e.getMessage());
            throw new CalculationException("Hesaplama sırasında aritmetik hata oluştu", e);
        } catch (IllegalArgumentException e) {
            log.error("Hesaplama için geçersiz parametre: {}", e.getMessage());
            throw new CalculationException("Hesaplama için geçersiz parametre: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Hesaplama sırasında beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new CalculationException("Hesaplama sırasında beklenmeyen bir hata oluştu", e);
        }
    }

    public StockSellOrderPreviewResponse createPreviewResponse(
            Account account,
            Stock stock,
            StockSellOrderRequest request,
            OrderCalculation calculation) {
        try {
            if (account == null || stock == null || calculation == null) {
                throw new IllegalArgumentException("Hesap, hisse senedi veya hesaplama bilgisi boş olamaz");
            }

            return getStockSellOrderPreviewResponse(account, stock, request, calculation.price(),
                    calculation.totalAmount(), calculation.commission(), calculation.bsmv(),
                    calculation.totalTaxAndCommission(), calculation.netAmount(), calculation);
        } catch (Exception e) {
            log.error("Önizleme yanıtı oluşturulurken bir hata oluştu: {}", e.getMessage());
            throw new CalculationException("Önizleme yanıtı oluşturulurken bir hata oluştu", e);
        }
    }

    static StockSellOrderPreviewResponse getStockSellOrderPreviewResponse(Account account, Stock stock, StockSellOrderRequest request, BigDecimal price, BigDecimal bigDecimal, BigDecimal commission, BigDecimal bsmv, BigDecimal bigDecimal2, BigDecimal bigDecimal3, OrderCalculation calculation) {
        return StockSellOrderPreviewResponse.builder()
                .accountNumber(account.getAccountNumber())
                .stockName(stock.getName())
                .stockSymbol(stock.getSymbol())
                .price(price)
                .quantity(request.getQuantity())
                .tradeDate(LocalDate.now())
                .valueDate("T+2")
                .totalAmount(bigDecimal)
                .stockGroup(stock.getGroup())
                .commission(commission)
                .bsmv(bsmv)
                .totalTaxAndCommission(bigDecimal2)
                .netAmount(bigDecimal3)
                .executionType(request.getExecutionType())
                .build();
    }

    public record OrderCalculation(
            BigDecimal price,
            BigDecimal commission,
            BigDecimal bsmv,
            BigDecimal totalTaxAndCommission,
            BigDecimal totalAmount,
            BigDecimal netAmount) {}
}
