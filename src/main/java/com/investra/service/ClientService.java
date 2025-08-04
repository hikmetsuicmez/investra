package com.investra.service;

import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;

import java.util.Optional;

public interface ClientService {
    Response<CreateClientResponse> createClient(CreateClientRequest createClientRequest, String userEmail);
    Optional<ClientSearchResponse> findClientByAnyIdentifier(
            String nationalityNumber,
            String passportNo,
            String blueCardNo,
            String taxNumber,
            String registrationNumber
    );
}
