package com.investra.service.helper;

import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.StockSellOrderPreviewResponse;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Stock;
import com.investra.enums.ClientType;
import com.investra.enums.ExecutionType;
import com.investra.enums.OrderType;
import com.investra.exception.CalculationException;
import com.investra.exception.ValidationException;
import com.investra.service.SimulationDateService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Component
@Slf4j
@Getter
@Setter
public class OrderCalculationService {

    private static final BigDecimal INDIVIDUAL_COMMISION_RATE = new BigDecimal("0.002"); // %0.2
    private static final BigDecimal CORPORATE_COMMISION_RATE = new BigDecimal("0.001"); // %0.1
    private static final BigDecimal BSMV_FEE_RATE = new BigDecimal("0.05"); // %5
    private static final int DECIMAL_SCALE = 2;

    private final SimulationDateService simulationDateService;

    @Autowired
    public OrderCalculationService(SimulationDateService simulationDateService) {
        this.simulationDateService = simulationDateService;
    }

    public OrderCalculation calculateOrderAmounts(
            Client client, Stock stock, int quantity, ExecutionType executionType,
            Double price, OrderType orderType) {
        try {
            if (client == null || stock == null) {
                throw new ValidationException();
            }

            BigDecimal commissionRate = client.getClientType() == ClientType.INDIVIDUAL
                    ? INDIVIDUAL_COMMISION_RATE
                    : CORPORATE_COMMISION_RATE;

            BigDecimal orderPrice = executionType == ExecutionType.MARKET
                    ? stock.getPrice()
                    : BigDecimal.valueOf(price);

            if (orderPrice == null || orderPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException();
            }

            BigDecimal quantityBD = BigDecimal.valueOf(quantity);

            BigDecimal totalAmount = orderPrice
                    .multiply(quantityBD)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal commission = totalAmount
                    .multiply(commissionRate)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal bsmv = commission
                    .multiply(BSMV_FEE_RATE)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal totalTaxAndCommission = commission.add(bsmv)
                    .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);

            BigDecimal netAmount;
            if (orderType == OrderType.SELL) {
                // Satış işlemi: Net tutar = Toplam tutar - komisyon ve vergiler
                netAmount = totalAmount
                        .subtract(totalTaxAndCommission)
                        .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
            } else {
                // Alış işlemi: Net tutar = Toplam tutar + komisyon ve vergiler
                netAmount = totalAmount
                        .add(totalTaxAndCommission)
                        .setScale(DECIMAL_SCALE, RoundingMode.HALF_UP);
            }

            // Simülasyon tarihini kullan
            LocalDate tradeDate = simulationDateService.getCurrentSimulationDate();

            return new OrderCalculation(
                    orderPrice,
                    quantity,
                    tradeDate,
                    "T+2",
                    commission,
                    bsmv,
                    totalTaxAndCommission,
                    totalAmount,
                    netAmount,
                    executionType);
        } catch (ArithmeticException e) {
            log.error("Hesaplama sırasında aritmetik hata oluştu: {}", e.getMessage());
            throw new CalculationException();
        } catch (IllegalArgumentException e) {
            log.error("Hesaplama için geçersiz parametre: {}", e.getMessage());
            throw new CalculationException();
        } catch (Exception e) {
            log.error("Hesaplama sırasında beklenmeyen bir hata oluştu: {}", e.getMessage());
            throw new CalculationException();
        }
    }

    public OrderCalculation calculateOrderAmounts(Client client, Stock stock, StockSellOrderRequest request) {
        return calculateOrderAmounts(
                client,
                stock,
                request.getQuantity(),
                request.getExecutionType(),
                request.getPrice() != null ? request.getPrice().doubleValue() : null,
                OrderType.SELL);
    }

    public StockSellOrderPreviewResponse createPreviewResponse(
            Account account,
            Stock stock,
            StockSellOrderRequest request,
            OrderCalculation calculation) {
        try {
            if (account == null || stock == null || calculation == null) {
                throw new ValidationException();
            }

            return getStockSellOrderPreviewResponse(account, stock, request, calculation.price(),
                    calculation.totalAmount(), calculation.commission(), calculation.bsmv(),
                    calculation.totalTaxAndCommission(), calculation.netAmount(), calculation);
        } catch (Exception e) {
            log.error("Önizleme yanıtı oluşturulurken bir hata oluştu: {}", e.getMessage());
            throw new CalculationException();
        }
    }

    private StockSellOrderPreviewResponse getStockSellOrderPreviewResponse(Account account, Stock stock,
            StockSellOrderRequest request, BigDecimal price, BigDecimal totalAmount, BigDecimal commission,
            BigDecimal bsmv, BigDecimal totalTaxAndCommission, BigDecimal netAmount, OrderCalculation calculation) {
        return StockSellOrderPreviewResponse.builder()
                .accountNumber(account.getAccountNumber())
                .stockName(stock.getName())
                .stockSymbol(stock.getCode())
                .price(price)
                .quantity(request.getQuantity())
                .tradeDate(LocalDate.now())
                .valueDate("T+2") // Genellikle T+2 gün sonra gerçekleşir
                .totalAmount(totalAmount)
                .stockGroup(stock.getGroup())
                .commission(commission)
                .bsmv(bsmv)
                .orderType(OrderType.SELL)
                .totalTaxAndCommission(totalTaxAndCommission)
                .netAmount(netAmount)
                .executionType(request.getExecutionType())
                .build();
    }

    public record OrderCalculation(
            BigDecimal price,
            int quantity,
            LocalDate tradeDate,
            String valueDate,
            BigDecimal commission,
            BigDecimal bsmv,
            BigDecimal totalTaxAndCommission,
            BigDecimal totalAmount,
            BigDecimal netAmount,
            ExecutionType executionType) {
    }

    // Benzersiz sipariş numarası oluşturur
    public String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" +
                (int) (Math.random() * 1000);
    }
}
