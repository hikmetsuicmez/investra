package com.investra.mapper;

import com.investra.dtos.response.ClientSearchResponse;
import com.investra.entity.Client;

public class ClientMapper {

    public static ClientSearchResponse mapToClientSearchResponse(Client client) {
        if (client == null) {
            return null;
        }
        return ClientSearchResponse.builder()
                .id(client.getId())
                .taxId(client.getTaxId())
                .nationalityNumber(client.getNationalityNumber())
                .phoneNumber(client.getPhone())
                .email(client.getEmail())
                .clientStatus(client.getStatus())
                .clientType(client.getClientType())
                .fullName(client.getFullName())
                .createdAt(client.getCreatedAt())
                .build();

    }
}
