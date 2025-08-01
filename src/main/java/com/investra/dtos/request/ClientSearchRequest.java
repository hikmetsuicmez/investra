package com.investra.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSearchRequest {
    private String searchTerm; // Müşteri No/T.C NO/Vergi Kimlik NO/Müşteri İsmi/Hesap No/Mavi Kart No
    private String searchType; // "CLIENT_NO", "TCKN", "VERGI_NO", "FULL_NAME", "ACCOUNT_NO", "BLUE_CARD_NO"
}
