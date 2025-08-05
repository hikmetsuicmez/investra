package com.investra.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientSearchRequest {
    private String searchTerm; // Müşteri no, Kimlik Numarası, Müşteri Adı, Soyadı gibi alanlarda arama yapılabilir.
    private String searchType; // Arama türü: "clientNo", "identityNo", "firstName", "lastName" gibi değerler alabilir.
    private Boolean isActive; // müşterinin aktif ya da pasifler arasında aranacağı seçilir
}
