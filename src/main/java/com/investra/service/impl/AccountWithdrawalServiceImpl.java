package com.investra.service.impl;

import com.investra.dtos.request.WithdrawalRequest;
import com.investra.dtos.response.WithdrawalResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Transaction;
import com.investra.entity.User;
import com.investra.enums.TransactionStatus;
import com.investra.enums.TransactionType;
import com.investra.exception.AccountNotFoundException;
import com.investra.exception.ClientNotFoundException;
import com.investra.exception.InvalidAmountException;
import com.investra.exception.UserNotFoundException;
import com.investra.exception.InsufficientBalanceException;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.TransactionRepository;
import com.investra.repository.UserRepository;
import com.investra.utils.ExceptionUtil;
import com.investra.service.AccountWithdrawalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountWithdrawalServiceImpl implements AccountWithdrawalService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Response<WithdrawalResponse> withdrawFromAccount(WithdrawalRequest request, String userEmail) {
        try {
            // Müşteri ve hesap kontrolleri
            Client client = findClientById(request.getClientId());
            Account account = findAccountById(request.getAccountId());
            User user = findUserByEmail(userEmail);

            // Müşteri-hesap ilişkisi kontrolü
            validateClientAccount(client, account);

            // Tutar kontrolü
            validateAmount(request.getAmount());

            // Kullanılabilir bakiye kontrolü (EN ÖNEMLİ KONTROL)
            validateAvailableBalance(account, request.getAmount());

            // İşlem tarihi kontrolü ve varsayılan değer atama
            LocalDate transactionDate = request.getTransactionDate() != null
                    ? request.getTransactionDate()
                    : LocalDate.now();

            // Mevcut bakiyeleri kaydet
            BigDecimal previousBalance = account.getBalance();
            BigDecimal previousAvailableBalance = account.getAvailableBalance();

            // Hesap bakiyelerini güncelle
            account.setBalance(previousBalance.subtract(request.getAmount()));
            account.setAvailableBalance(previousAvailableBalance.subtract(request.getAmount()));

            // Balance validation - negatif olamaz (setter'da da kontrol ediliyor ama ekstra
            // güvenlik için)
            if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                account.setBalance(BigDecimal.ZERO);
            }
            if (account.getAvailableBalance().compareTo(BigDecimal.ZERO) < 0) {
                account.setAvailableBalance(BigDecimal.ZERO);
            }

            // Balance ve AvailableBalance tutarlılık kontrolü
            if (account.getBalance().compareTo(account.getAvailableBalance()) < 0) {
                // Balance, AvailableBalance'dan küçük olamaz
                account.setBalance(account.getAvailableBalance());
            }

            // Hesabı kaydet
            accountRepository.save(account);

            // İşlem kaydı oluştur
            Transaction transaction = Transaction.builder()
                    .client(client)
                    .account(account)
                    .user(user)
                    .transactionType(TransactionType.WITHDRAWAL)
                    .amount(request.getAmount())
                    .previousBalance(previousBalance)
                    .newBalance(account.getBalance())
                    .description(request.getDescription())
                    .transactionDate(transactionDate)
                    .executedAt(LocalDateTime.now())
                    .status(TransactionStatus.COMPLETED)
                    .build();

            // İşlem kaydını kaydet
            transaction = transactionRepository.save(transaction);

            // Yanıt oluştur
            WithdrawalResponse response = createWithdrawalResponse(
                    transaction.getId(), account, client, request.getAmount(),
                    previousBalance, account.getBalance(),
                    previousAvailableBalance, account.getAvailableBalance(),
                    request.getDescription(), transactionDate, transaction.getExecutedAt());

            log.info("Hesaptan bakiye çıkışı işlemi başarılı: Müşteri={}, Hesap={}, Tutar={}",
                    client.getFullName(), account.getAccountNumber(), request.getAmount());

            return Response.<WithdrawalResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .message("Bakiye çıkış işlemi başarıyla tamamlandı")
                    .data(response)
                    .build();

        } catch (Exception e) {
            log.error("Bakiye çıkışı hatası: {}", e.getMessage(), e);

            return Response.<WithdrawalResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .message(e.getMessage())
                    .errorCode(ExceptionUtil.getErrorCode(e))
                    .build();
        }
    }

    // Yardımcı metotlar

    private Client findClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private void validateClientAccount(Client client, Account account) {
        if (!account.getClient().getId().equals(client.getId())) {
            throw new AccountNotFoundException(account.getId());
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException();
        }
    }

    // Kullanılabilir bakiye kontrolü - EN ÖNEMLİ KONTROL
    private void validateAvailableBalance(Account account, BigDecimal withdrawalAmount) {
        if (account.getAvailableBalance().compareTo(withdrawalAmount) < 0) {
            throw new InsufficientBalanceException();
        }
    }

    private WithdrawalResponse createWithdrawalResponse(
            Long transactionId, Account account, Client client,
            BigDecimal amount, BigDecimal previousBalance, BigDecimal newBalance,
            BigDecimal previousAvailableBalance, BigDecimal newAvailableBalance,
            String description, LocalDate transactionDate, LocalDateTime executedAt) {

        return WithdrawalResponse.builder()
                .transactionId(transactionId)
                .accountNumber(account.getAccountNumber())
                .clientName(client.getFullName())
                .currencyCode(account.getCurrency().name())
                .amount(amount)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .previousAvailableBalance(previousAvailableBalance)
                .newAvailableBalance(newAvailableBalance)
                .description(description)
                .transactionDate(transactionDate)
                .executedAt(executedAt)
                .build();
    }
}
