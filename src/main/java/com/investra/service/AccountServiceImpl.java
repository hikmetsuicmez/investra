package com.investra.service;

import com.investra.dtos.request.AccountCreationRequest;
import com.investra.dtos.request.ClientSearchForAccountRequest;
import com.investra.dtos.response.AccountResponse;
import com.investra.dtos.response.ClientForAccountResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.enums.AccountType;
import com.investra.exception.AccountAlreadyExistsException;
import com.investra.exception.ClientNotFoundException;
import com.investra.mapper.AccountMapper;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.sun.jdi.request.InvalidRequestStateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    // @CacheEvict(value = "accounts", allEntries = true)
    public Response<AccountResponse> createAccount(AccountCreationRequest request) {
        log.info("Yeni hesap oluşturma isteği alındı. Müşteri ID: {}", request.getClientId());

        // Müşteri kontrolü
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> {
                    log.error("Müşteri bulunamadı. ID: {}", request.getClientId());
                    return new ClientNotFoundException("Müşteri bulunamadı. ID: " + request.getClientId());
                });

        // IBAN kontrolü
        if (accountRepository.existsByIban(request.getIban())) {
            log.error("Bu IBAN ile kayıtlı bir hesap zaten var. IBAN: {}", request.getIban());
            throw new AccountAlreadyExistsException("IBAN", request.getIban());
        }

        // Aracı kurum hesap numarası kontrolü
        if (accountRepository.existsByAccountNumberAtBroker(request.getAccountNumberAtBroker())) {
            log.error("Bu hesap numarası ile kayıtlı bir hesap zaten var. Hesap No: {}", request.getAccountNumberAtBroker());
            throw new AccountAlreadyExistsException("Hesap Numarası", request.getAccountNumberAtBroker());
        }

        // Yeni hesap oluştur
        Account account = Account.builder()
                .client(client)
                .nickname(request.getNickname())
                .accountType(request.getAccountType())
                .currency(request.getCurrency())
                .brokerName(request.getBrokerName())
                .brokerCode(request.getBrokerCode())
                .custodianName(request.getCustodianName())
                .custodianCode(request.getCustodianCode())
                .iban(request.getIban())
                .accountNumberAtBroker(request.getAccountNumberAtBroker())
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .availableBalance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        // Eğer hesap Takas Hesabı ise ve müşterinin başka takas hesabı yoksa, bu hesabı primary olarak işaretle
        if (request.getAccountType() == AccountType.SETTLEMENT) {
            List<Account> existingSettlementAccounts = accountRepository.findByClientIdAndAccountTypeAndCurrency(
                    client.getId(), AccountType.SETTLEMENT, request.getCurrency());

            if (existingSettlementAccounts.isEmpty()) {
                account.setPrimarySettlement(true);
                log.info("Müşteri için ilk {} takas hesabı. Primary olarak işaretlendi.", request.getCurrency());
            }
        }

        // Hesabı kaydet
        account = accountRepository.save(account);
        log.info("Yeni hesap başarıyla oluşturuldu. Hesap ID: {}", account.getId());

        AccountResponse accountResponse = AccountMapper.toAccountResponse(account);

        return Response.<AccountResponse>builder()
                .statusCode(HttpStatus.CREATED.value())
                .isSuccess(true)
                .message("Hesap başarıyla oluşturuldu")
                .data(accountResponse)
                .build();
    }

    @Override
    @Cacheable(value = "accounts", key = "'client_' + #clientId")
    public Response<List<AccountResponse>> getAccountsByClientId(Long clientId) {
        log.info("Müşterinin hesapları isteniyor. Müşteri ID: {}", clientId);

        // Müşteri var mı kontrol et
        if (!clientRepository.existsById(clientId)) {
            log.error("Müşteri bulunamadı. ID: {}", clientId);
            throw new ClientNotFoundException("Müşteri bulunamadı. ID: " + clientId);
        }

        List<Account> accounts = accountRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        List<AccountResponse> accountResponses = accounts.stream()
                .map(AccountMapper::toAccountResponse)
                .collect(Collectors.toList());

        return Response.<List<AccountResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Müşteri hesapları başarıyla getirildi")
                .data(accountResponses)
                .build();
    }

    @Override
    @Cacheable(value = "accounts", key = "#id")
    public Response<AccountResponse> getAccountById(Long id) {
        log.info("Hesap detayları isteniyor. Hesap ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Hesap bulunamadı. ID: {}", id);
                    return new InvalidRequestStateException("Hesap bulunamadı. ID: " + id);
                });

        AccountResponse accountResponse = AccountMapper.toAccountResponse(account);

        return Response.<AccountResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Hesap detayları başarıyla getirildi")
                .data(accountResponse)
                .build();
    }

    @Override
    public Response<List<ClientForAccountResponse>> searchClientsForAccount(ClientSearchForAccountRequest request) {
        log.info("Hesap açılışı için müşteri araması yapılıyor. Arama terimi: {}, Arama tipi: {}",
                request.getSearchTerm(), request.getSearchType());

        List<Client> clients;

        // Arama kriterlerine göre müşterileri getir
        if (request.getSearchTerm() == null || request.getSearchTerm().trim().isEmpty()) {
            // Arama terimi boş ise en son eklenen müşterileri getir
            clients = clientRepository.findTop20ByOrderByCreatedAtDesc();
        } else {
            // Arama tipine göre müşteri ara
            switch (request.getSearchType()) {
                case "TCKN":
                    clients = clientRepository.findByNationalityNumberContaining(request.getSearchTerm())
                            .map(List::of).orElse(List.of());
                    break;
                case "VKN":
                    clients = clientRepository.findByTaxIdContaining(request.getSearchTerm())
                            .map(List::of).orElse(List.of());
                    break;
                case "PASSPORT_NO":
                    clients = clientRepository.findByPassportNoContaining(request.getSearchTerm())
                            .map(List::of).orElse(List.of());
                    break;
                case "BLUE_CARD_NO":
                    clients = clientRepository.findByBlueCardNoContaining(request.getSearchTerm())
                            .map(List::of).orElse(List.of());
                    break;
                case "ALL":
                default:
                    // Tüm alanlarda ara
                    Optional<Client> byNationality = clientRepository.findByNationalityNumberContaining(request.getSearchTerm());
                    Optional<Client> byTaxId = clientRepository.findByTaxIdContaining(request.getSearchTerm());
                    Optional<Client> byPassport = clientRepository.findByPassportNoContaining(request.getSearchTerm());
                    Optional<Client> byBlueCard = clientRepository.findByBlueCardNoContaining(request.getSearchTerm());

                    clients = List.of(
                            byNationality.orElse(null),
                            byTaxId.orElse(null),
                            byPassport.orElse(null),
                            byBlueCard.orElse(null)
                    ).stream()
                            .filter(c -> c != null)
                            .collect(Collectors.toList());
                    break;
            }
        }

        // Müşterileri DTO'ya dönüştür ve her müşteri için hesap sayısını ekle
        List<ClientForAccountResponse> clientResponses = clients.stream()
                .map(client -> {
                    ClientForAccountResponse response = mapClientToResponse(client);
                    int accountCount = accountRepository.countAccountsByClientId(client.getId());
                    response.setAccountCount(accountCount);
                    return response;
                })
                .collect(Collectors.toList());

        return Response.<List<ClientForAccountResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Müşteri araması başarıyla tamamlandı")
                .data(clientResponses)
                .build();
    }

    @Override
    public Response<List<ClientForAccountResponse>> getRecentClients(int limit) {
        log.info("Son eklenen {} müşteri isteniyor", limit);

        if (limit <= 0) {
            limit = 20;  // Varsayılan limit
        }

        List<Client> clients = clientRepository.findTopNByOrderByCreatedAtDesc(limit);

        // Müşterileri DTO'ya dönüştür ve her müşteri için hesap sayısını ekle
        List<ClientForAccountResponse> clientResponses = clients.stream()
                .map(client -> {
                    ClientForAccountResponse response = mapClientToResponse(client);
                    int accountCount = accountRepository.countAccountsByClientId(client.getId());
                    response.setAccountCount(accountCount);
                    return response;
                })
                .collect(Collectors.toList());

        return Response.<List<ClientForAccountResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Son eklenen müşteriler başarıyla getirildi")
                .data(clientResponses)
                .build();
    }

    // Yardımcı metot: Client'ı ClientForAccountResponse'a dönüştürür
    private ClientForAccountResponse mapClientToResponse(Client client) {
        return ClientForAccountResponse.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .nationalityNumber(client.getNationalityNumber())
                .taxId(client.getTaxId())
                .passportNo(client.getPassportNo())
                .blueCardNo(client.getBlueCardNo())
                .email(client.getEmail())
                .phone(client.getPhone())
                .status(client.getStatus())
                .clientType(client.getClientType())
                .createdAt(client.getCreatedAt())
                .build();
    }
}
