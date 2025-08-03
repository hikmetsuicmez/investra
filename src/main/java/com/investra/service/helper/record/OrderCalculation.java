package com.investra.service.helper.record;

import java.math.BigDecimal;

public record OrderCalculation(
        BigDecimal unitPrice,
        BigDecimal commission,
        BigDecimal bsmv,
        BigDecimal totalTaxAndCommission,
        BigDecimal totalAmount,
        BigDecimal netAmount
) {}
