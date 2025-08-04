package com.investra.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientSearchForAccountRequest {

    private String searchTerm;

    @Builder.Default
    private String searchType = "ALL"; // ALL, TCKN, VKN, PASSPORT_NO, BLUE_CARD_NO
}
