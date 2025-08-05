package com.investra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investra.constants.ApiEndpoints;
import com.investra.docs.ClientApiDocs;
import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;
import com.investra.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(ApiEndpoints.Client.BASE)
@RequiredArgsConstructor
public class ClientController implements ClientApiDocs {
    @Autowired
    ObjectMapper objectMapper;
    private final ClientService clientService;

    @PostMapping(ApiEndpoints.Client.CREATE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<CreateClientResponse>> createUser(@RequestBody Map<String, Object> payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        String clientTypeValue = String.valueOf(payload.get("clientType"));

        CreateClientRequest request;

        if ("INDIVIDUAL".equalsIgnoreCase(clientTypeValue)) {
            request = objectMapper.convertValue(payload, CreateIndividualClientRequest.class);
        } else if ("CORPORATE".equalsIgnoreCase(clientTypeValue)) {
            request = objectMapper.convertValue(payload, CreateCorporateClientRequest.class);
        } else {
            return ResponseEntity.badRequest().body(
                    Response.<CreateClientResponse>builder()
                            .statusCode(400)
                            .message("Geçersiz müşteri tipi")
                            .build()
            );
        }

        Response<CreateClientResponse> response = clientService.createClient(request, userEmail);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping(ApiEndpoints.Client.GET_CLIENT_INFO_BY_ID)
    public ResponseEntity<Response<ClientSearchResponse>> findClient(
            @RequestParam(required = false) String nationalityNumber,
            @RequestParam(required = false) String passportNo,
            @RequestParam(required = false) String blueCardNo,
            @RequestParam(required = false) String taxNumber,
            @RequestParam(required = false) String registrationNumber
    ) {
        Optional<ClientSearchResponse> response = clientService.findClientByAnyIdentifier(
                nationalityNumber, passportNo, blueCardNo, taxNumber, registrationNumber
        );

        if (response.isEmpty()) {
            return ResponseEntity.status(404).body(
                    Response.<ClientSearchResponse>builder()
                            .statusCode(404)
                            .message("Müşteri bulunamadı")
                            .build()
            );
        }

        return ResponseEntity.ok(
                Response.<ClientSearchResponse>builder()
                        .statusCode(200)
                        .message("Müşteri bulundu")
                        .data(response.get())
                        .build()
        );
    }
}

