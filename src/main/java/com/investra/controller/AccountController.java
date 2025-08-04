package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.request.AccountCreationRequest;
import com.investra.dtos.request.ClientSearchForAccountRequest;
import com.investra.dtos.response.AccountResponse;
import com.investra.dtos.response.ClientForAccountResponse;
import com.investra.dtos.response.Response;
import com.investra.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Account.BASE)
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    // Yeni hesap oluşturur
    @PostMapping(ApiEndpoints.Account.CREATE)
    public ResponseEntity<Response<AccountResponse>> createAccount(
            @Valid @RequestBody AccountCreationRequest request) {
        log.info("Hesap oluşturma isteği alındı");
        return ResponseEntity.ok(accountService.createAccount(request));
    }

    // Hesap ID'sine göre hesap detaylarını getirir
    @GetMapping(ApiEndpoints.Account.GET_BY_ID)
    public ResponseEntity<Response<AccountResponse>> getAccountById(
            @PathVariable Long accountId) {
        log.info("Hesap detayları isteniyor. ID: {}", accountId);
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    // Müşteri ID'sine göre müşterinin tüm hesaplarını getirir
    @GetMapping(ApiEndpoints.Account.GET_BY_CLIENT)
    public ResponseEntity<Response<List<AccountResponse>>> getAccountsByClientId(
            @PathVariable Long clientId) {
        log.info("Müşteri hesapları isteniyor. Müşteri ID: {}", clientId);
        return ResponseEntity.ok(accountService.getAccountsByClientId(clientId));
    }

    // Hesap açılışı için müşteri arama
    @PostMapping(ApiEndpoints.Account.SEARCH_CLIENTS)
    public ResponseEntity<Response<List<ClientForAccountResponse>>> searchClientsForAccount(
            @RequestBody ClientSearchForAccountRequest request) {
        log.info("Hesap açılışı için müşteri araması yapılıyor");
        return ResponseEntity.ok(accountService.searchClientsForAccount(request));
    }

    // En son eklenen müşterileri getirir (hesap açılışı sayfası için)
    @GetMapping(ApiEndpoints.Account.RECENT_CLIENTS)
    public ResponseEntity<Response<List<ClientForAccountResponse>>> getRecentClients(
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Son eklenen {} müşteri isteniyor", limit);
        return ResponseEntity.ok(accountService.getRecentClients(limit));
    }
}
