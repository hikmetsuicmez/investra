package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.request.DepositRequest;
import com.investra.dtos.response.DepositResponse;
import com.investra.dtos.response.Response;
import com.investra.service.AccountDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiEndpoints.Account.BASE + ApiEndpoints.Account.DEPOSIT)
@RequiredArgsConstructor
@Slf4j
public class AccountDepositController {

    private final AccountDepositService accountDepositService;

    @PostMapping
    public ResponseEntity<Response<DepositResponse>> depositToAccount(
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Bakiye yükleme isteği alındı: ClientId={}, AccountId={}, Amount={}",
                request.getClientId(), request.getAccountId(), request.getAmount());

        Response<DepositResponse> response = accountDepositService.depositToAccount(request, userDetails.getUsername());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
