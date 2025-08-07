package com.investra.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PortfolioCreateRequest {
    
    private Long clientId;
}
