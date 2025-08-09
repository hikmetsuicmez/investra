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
public class SimulationStatusResponse {

    private LocalDate currentSimulationDate;
    private LocalDate initialDate;
    private LocalDate realDate;
    private Integer daysAdvanced;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedAt;
    private String description;
    private boolean isAdvanced;

    private String status;
    private String message;
}
