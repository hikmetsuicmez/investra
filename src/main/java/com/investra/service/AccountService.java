package com.investra.service;

import com.investra.dtos.request.AccountCreationRequest;
import com.investra.dtos.request.ClientSearchForAccountRequest;
import com.investra.dtos.response.AccountResponse;
import com.investra.dtos.response.ClientForAccountResponse;
import com.investra.dtos.response.Response;

import java.util.List;

public interface AccountService {

    // Yeni hesap oluşturur
    Response<AccountResponse> createAccount(AccountCreationRequest request);

    // Hesap ID'sine göre hesap detaylarını getirir
    Response<AccountResponse> getAccountById(Long accountId);

    // Müşteri ID'sine göre müşterinin tüm hesaplarını getirir
    Response<List<AccountResponse>> getAccountsByClientId(Long clientId);

    // Hesap açılışı için müşteri arama
    Response<List<ClientForAccountResponse>> searchClientsForAccount(ClientSearchForAccountRequest request);

    // En son eklenen müşterileri tarihe göre getirir
    Response<List<ClientForAccountResponse>> getRecentClients(int limit);
}
