package com.investra.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndOfDayStatusResponse {
    private boolean pricesUpdated;
    private LocalDateTime lastPriceUpdateTime;
    private LocalDate valuationDate;
    private boolean valuationCompleted;
    private String statusMessage;
}
