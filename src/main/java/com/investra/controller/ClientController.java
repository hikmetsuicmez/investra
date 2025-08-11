package com.investra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.investra.constants.ApiEndpoints;
import com.investra.docs.ClientApiDocs;
import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.request.UpdateClientRequest;
import com.investra.dtos.request.UpdateCorporateClientRequest;
import com.investra.dtos.request.UpdateIndividualClientRequest;
import com.investra.dtos.response.ClientDTO;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateClientResponse;
import com.investra.entity.Client;
import com.investra.exception.ErrorCode;
import com.investra.service.ClientService;
import com.investra.service.StockBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ApiEndpoints.Client.BASE)
@RequiredArgsConstructor
@Slf4j
public class ClientController implements ClientApiDocs {

    @Autowired
    private final ObjectMapper objectMapper;
    private final ClientService clientService;
    private final StockBuyService stockBuyService;

    @Override
    @PostMapping(ApiEndpoints.Client.CREATE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<CreateClientResponse>> createClient(@RequestBody Map<String, Object> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            String clientTypeValue = String.valueOf(payload.get("clientType"));

            if (clientTypeValue == null || "null".equals(clientTypeValue)) {
                return ResponseEntity.badRequest().body(
                        Response.<CreateClientResponse>builder()
                                .statusCode(400)
                                .message("Müşteri tipi belirtilmelidir (INDIVIDUAL veya CORPORATE)")
                                .build());
            }

            CreateClientRequest request;

            if ("INDIVIDUAL".equalsIgnoreCase(clientTypeValue)) {
                request = objectMapper.convertValue(payload, CreateIndividualClientRequest.class);
            } else if ("CORPORATE".equalsIgnoreCase(clientTypeValue)) {
                request = objectMapper.convertValue(payload, CreateCorporateClientRequest.class);
            } else {
                return ResponseEntity.badRequest().body(
                        Response.<CreateClientResponse>builder()
                                .statusCode(400)
                                .message("Geçersiz müşteri tipi. INDIVIDUAL veya CORPORATE olmalıdır")
                                .build());
            }

            // Validation yap
            validateCreateClientRequest(request);

            Response<CreateClientResponse> response = clientService.createClient(request, userEmail);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            log.error("Müşteri oluşturma hatası: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    Response.<CreateClientResponse>builder()
                            .statusCode(400)
                            .message("Müşteri oluşturulamadı: " + e.getMessage())
                            .build());
        }
    }

    @Override
    @PutMapping(ApiEndpoints.Client.UPDATE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<UpdateClientResponse>> updateClient(
            @PathVariable Long clientId,
            @RequestBody Map<String, Object> payload) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            String clientTypeValue = String.valueOf(payload.get("clientType"));

            if (clientTypeValue == null || "null".equals(clientTypeValue)) {
                return ResponseEntity.badRequest().body(
                        Response.<UpdateClientResponse>builder()
                                .statusCode(400)
                                .message("Müşteri tipi belirtilmelidir (INDIVIDUAL veya CORPORATE)")
                                .build());
            }

            UpdateClientRequest request;

            if ("INDIVIDUAL".equalsIgnoreCase(clientTypeValue)) {
                request = objectMapper.convertValue(payload, UpdateIndividualClientRequest.class);
            } else if ("CORPORATE".equalsIgnoreCase(clientTypeValue)) {
                request = objectMapper.convertValue(payload, UpdateCorporateClientRequest.class);
            } else {
                return ResponseEntity.badRequest().body(
                        Response.<UpdateClientResponse>builder()
                                .statusCode(400)
                                .message("Geçersiz müşteri tipi. INDIVIDUAL veya CORPORATE olmalıdır")
                                .errorCode(ErrorCode.VALIDATION_ERROR)
                                .build());
            }

            // Validation yap
            validateUpdateClientRequest(request);

            Response<UpdateClientResponse> response = clientService.updateClient(clientId, request, userEmail);
            return ResponseEntity.status(response.getStatusCode()).body(response);
        } catch (Exception e) {
            log.error("Müşteri güncelleme hatası: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                    Response.<UpdateClientResponse>builder()
                            .statusCode(400)
                            .message("Müşteri güncellenemedi: " + e.getMessage())
                            .build());
        }
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
                        .build());
    }

    @Override
    @PatchMapping(ApiEndpoints.Client.DELETE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRADER')")
    public ResponseEntity<Response<Void>> deleteClient(@RequestBody ClientSearchRequest request) {
        Response<List<ClientSearchResponse>> searchResponse = clientService.searchClients(request);
        log.info("Dönen response: {}", searchResponse);

        List<ClientSearchResponse> clients = searchResponse.getData();
        log.info("Dönen müşteri listesi: {}", clients);

        if (clients == null || clients.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Response.<Void>builder()
                            .statusCode(404)
                            .message("Müşteri bulunamadı.")
                            .build());
        }

        ClientSearchResponse clientToDelete = clients.get(0);
        log.info("silinecek müşteri: {}", clientToDelete);

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

    /**
     * Müşteri oluşturma isteğini validate eder
     */
    private void validateCreateClientRequest(CreateClientRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Müşteri bilgileri boş olamaz");
        }

        if (request.getClientType() == null) {
            throw new IllegalArgumentException("Müşteri tipi belirtilmelidir");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email adresi zorunludur");
        }

        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Telefon numarası zorunludur");
        }

        if (request.getTaxType() == null) {
            throw new IllegalArgumentException("Vergi tipi zorunludur");
        }

        // Bireysel müşteri validasyonu
        if (request instanceof CreateIndividualClientRequest individualRequest) {
            if (individualRequest.getFullName() == null || individualRequest.getFullName().trim().isEmpty()) {
                throw new IllegalArgumentException("Müşteri adı zorunludur");
            }
            if (!individualRequest.isIdentificationProvided()) {
                throw new IllegalArgumentException(
                        "TCKN, Pasaport No, Yabancı Kimlik No veya Mavi Kart No alanlarından en az biri girilmelidir");
            }
        }

        // Kurumsal müşteri validasyonu
        if (request instanceof CreateCorporateClientRequest corporateRequest) {
            if (corporateRequest.getCompanyName() == null || corporateRequest.getCompanyName().trim().isEmpty()) {
                throw new IllegalArgumentException("Şirket adı zorunludur");
            }
            if (corporateRequest.getTaxNumber() == null || corporateRequest.getTaxNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Vergi numarası zorunludur");
            }
            if (corporateRequest.getCompanyType() == null || corporateRequest.getCompanyType().trim().isEmpty()) {
                throw new IllegalArgumentException("Şirket türü zorunludur");
            }
            if (corporateRequest.getSector() == null || corporateRequest.getSector().trim().isEmpty()) {
                throw new IllegalArgumentException("Faaliyet alanı zorunludur");
            }
        }
    }

    /**
     * Müşteri güncelleme isteğini validate eder
     */
    private void validateUpdateClientRequest(UpdateClientRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Müşteri bilgileri boş olamaz");
        }

        if (request.getClientType() == null) {
            throw new IllegalArgumentException("Müşteri tipi belirtilmelidir");
        }

        // Email validasyonu (opsiyonel ama girilirse geçerli olmalı)
        if (request.getEmail() != null && request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email adresi boş olamaz");
        }

        // Telefon validasyonu (opsiyonel ama girilirse geçerli olmalı)
        if (request.getPhone() != null && request.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Telefon numarası boş olamaz");
        }

        // Bireysel müşteri validasyonu
        if (request instanceof UpdateIndividualClientRequest individualRequest) {
            if (individualRequest.getFullName() != null && individualRequest.getFullName().trim().isEmpty()) {
                throw new IllegalArgumentException("Müşteri adı boş olamaz");
            }
            if (individualRequest.getNationalityNumber() != null
                    && individualRequest.getNationalityNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("TCKN boş olamaz");
            }
            if (individualRequest.getPassportNo() != null && individualRequest.getPassportNo().trim().isEmpty()) {
                throw new IllegalArgumentException("Pasaport numarası boş olamaz");
            }
            if (individualRequest.getBlueCardNo() != null && individualRequest.getBlueCardNo().trim().isEmpty()) {
                throw new IllegalArgumentException("Mavi kart numarası boş olamaz");
            }
            if (individualRequest.getTaxId() != null && individualRequest.getTaxId().trim().isEmpty()) {
                throw new IllegalArgumentException("Vergi numarası boş olamaz");
            }
        }

        // Kurumsal müşteri validasyonu
        if (request instanceof UpdateCorporateClientRequest corporateRequest) {
            if (corporateRequest.getCompanyName() != null && corporateRequest.getCompanyName().trim().isEmpty()) {
                throw new IllegalArgumentException("Şirket adı boş olamaz");
            }
            if (corporateRequest.getTaxNumber() != null && corporateRequest.getTaxNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Vergi numarası boş olamaz");
            }
            if (corporateRequest.getRegistrationNumber() != null
                    && corporateRequest.getRegistrationNumber().trim().isEmpty()) {
                throw new IllegalArgumentException("Sicil numarası boş olamaz");
            }
            if (corporateRequest.getCompanyType() != null && corporateRequest.getCompanyType().trim().isEmpty()) {
                throw new IllegalArgumentException("Şirket türü boş olamaz");
            }
            if (corporateRequest.getSector() != null && corporateRequest.getSector().trim().isEmpty()) {
                throw new IllegalArgumentException("Faaliyet alanı boş olamaz");
            }
        }
    }
}
