package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.AccountWithdrawalApiDocs;
import com.investra.dtos.request.WithdrawalRequest;
import com.investra.dtos.response.WithdrawalResponse;
import com.investra.dtos.response.Response;
import com.investra.service.AccountWithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiEndpoints.Account.BASE + ApiEndpoints.Account.WITHDRAWAL)
@RequiredArgsConstructor
@Slf4j
public class AccountWithdrawalController implements AccountWithdrawalApiDocs {

    private final AccountWithdrawalService accountWithdrawalService;

    @PostMapping
    public ResponseEntity<Response<WithdrawalResponse>> withdrawFromAccount(
            @Valid @RequestBody WithdrawalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Bakiye çıkışı isteği alındı: ClientId={}, AccountId={}, Amount={}",
                request.getClientId(), request.getAccountId(), request.getAmount());

        Response<WithdrawalResponse> response = accountWithdrawalService.withdrawFromAccount(request, userDetails.getUsername());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
