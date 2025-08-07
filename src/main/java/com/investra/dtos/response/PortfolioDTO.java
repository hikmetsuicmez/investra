package com.investra.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class PortfolioDTO {

    private Long id;
    private Long clientId;
    private LocalDateTime createdAt;
}
