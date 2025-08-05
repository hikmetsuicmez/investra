package com.investra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investra.constants.ApiEndpoints;
import com.investra.docs.ClientApiDocs;
import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.response.ClientDTO;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Client;
import com.investra.service.ClientService;
import com.investra.service.StockBuyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping(ApiEndpoints.Client.BASE)
@RequiredArgsConstructor
public class ClientController implements ClientApiDocs {
    @Autowired
    ObjectMapper objectMapper;
    private final ClientService clientService;
    private final StockBuyService stockBuyService;

    @Override
    @PostMapping(ApiEndpoints.Client.CREATE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<CreateClientResponse>> createClient(@RequestBody Map<String, Object> payload) {
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

    @Override
    @PostMapping(ApiEndpoints.Client.GET_CLIENT_INFO_BY_ID)
    public ResponseEntity<Response<ClientSearchResponse>> findClient(@RequestBody ClientSearchRequest request) {

        Response<List<ClientSearchResponse>> searchResponse = clientService.searchClients(request);

        List<ClientSearchResponse> clients = searchResponse.getData();

        if (clients == null || clients.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.<ClientSearchResponse>builder()
                            .statusCode(404)
                            .message("Müşteri bulunamadı.")
                            .build());
        }

        ClientSearchResponse client = clients.get(0);

        return ResponseEntity.ok(
                Response.<ClientSearchResponse>builder()
                        .statusCode(200)
                        .message("Müşteri bulundu.")
                        .data(client)
                        .build()
        );
    }

    @Override
    @PatchMapping(ApiEndpoints.Client.DELETE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<Void>> deleteClient(@RequestBody ClientSearchRequest request) {

        Response<List<ClientSearchResponse>> searchResponse = clientService.searchClients(request);

        List<ClientSearchResponse> clients = searchResponse.getData();

        if (clients == null || clients.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.<Void>builder()
                            .statusCode(404)
                            .message("Müşteri bulunamadı.")
                            .build());
        }

        ClientSearchResponse clientToDelete = clients.get(0);

        Client client = clientService.findEntityById(clientToDelete.getId());
        Response<Void> result = clientService.deleteClient(client);

        return ResponseEntity.status(result.getStatusCode()).body(result);
    }

    @Override
    @GetMapping(ApiEndpoints.Client.ACTIVE_LIST)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<List<ClientDTO>>> getActiveClients() {
        Response<List<ClientDTO>> response = clientService.getActiveClients();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Override
    @GetMapping(ApiEndpoints.Client.PASSIVE_LIST)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<List<ClientDTO>>> getInactiveClients() {
        Response<List<ClientDTO>> response = clientService.getInactiveClients();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


}

