package com.investra.service.helper;

import com.investra.dtos.request.StockOrderRequest;
import com.investra.dtos.response.StockOrderPreviewResponse;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Stock;
import com.investra.enums.ClientType;
import com.investra.enums.ExecutionType;
import com.investra.exception.CalculationException;
import com.investra.service.helper.record.OrderCalculation;
import com.investra.service.helper.record.OrderEntities;
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

    public OrderCalculation calculateOrderAmounts(Client client, Stock stock, StockOrderRequest request) {
        try {
            if (client == null || stock == null) {
                throw new IllegalArgumentException("Müşteri veya hisse senedi bilgisi boş olamaz");
            }
            BigDecimal commissionRate = client.getClientType() == ClientType.INDIVIDUAL
                    ? INDIVIDUAL_COMMISION_RATE
                    : CORPORATE_COMMISION_RATE;

            BigDecimal unitPrice = request.getExecutionType() == ExecutionType.MARKET
                    ? stock.getCurrentPrice()
                    : request.getPrice();

            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Geçersiz fiyat: " + unitPrice);
            }

            BigDecimal quantity = BigDecimal.valueOf(request.getQuantity());

            BigDecimal commission = unitPrice
                    .multiply(quantity)
                    .multiply(commissionRate)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal bsmv = commission
                    .multiply(BSMV_FEE_RATE)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal totalTaxAndCommission = commission.add(bsmv)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal totalAmount = unitPrice
                    .multiply(quantity)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal netAmount = totalAmount
                    .subtract(totalTaxAndCommission)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            return new OrderCalculation(
                    unitPrice,
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

    public StockOrderPreviewResponse createPreviewResponse(
            Account account,
            Stock stock,
            StockOrderRequest request,
            OrderCalculation calculation) {
        try {
            if (account == null || stock == null || calculation == null) {
                throw new IllegalArgumentException("Hesap, hisse senedi veya hesaplama bilgisi boş olamaz");
            }

            return getStockOrderPreviewResponse(account, stock, request, calculation.unitPrice(),
                    calculation.totalAmount(), calculation.commission(), calculation.bsmv(),
                    calculation.totalTaxAndCommission(), calculation.netAmount(), calculation);
        } catch (Exception e) {
            log.error("Önizleme yanıtı oluşturulurken bir hata oluştu: {}", e.getMessage());
            throw new CalculationException("Önizleme yanıtı oluşturulurken bir hata oluştu", e);
        }
    }

    static StockOrderPreviewResponse getStockOrderPreviewResponse(Account account, Stock stock, StockOrderRequest request, BigDecimal unitPrice, BigDecimal bigDecimal, BigDecimal commission, BigDecimal bsmv, BigDecimal bigDecimal2, BigDecimal bigDecimal3, OrderCalculation calculation) {
        return StockOrderPreviewResponse.builder()
                .accountNumber(account.getAccountNumber())
                .stockName(stock.getName())
                .stockSymbol(stock.getSymbol())
                .price(unitPrice)
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

    public OrderCalculation getCalculation(StockOrderRequest request, OrderEntities entities) {
        return calculateOrderAmounts(
                entities.client(), entities.stock(), request);
    }

}



