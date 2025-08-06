package com.investra.service;

import com.investra.dtos.request.DepositRequest;
import com.investra.dtos.response.DepositResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Transaction;
import com.investra.entity.User;
import com.investra.enums.TransactionStatus;
import com.investra.enums.TransactionType;
import com.investra.exception.ErrorCode;
import com.investra.exception.AccountNotFoundException;
import com.investra.exception.ClientNotFoundException;
import com.investra.exception.InvalidAmountException;
import com.investra.exception.UserNotFoundException;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.TransactionRepository;
import com.investra.repository.UserRepository;
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
public class AccountDepositServiceImpl implements AccountDepositService {

    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public Response<DepositResponse> depositToAccount(DepositRequest request, String userEmail) {
        try {
            // Müşteri ve hesap kontrolleri
            Client client = findClientById(request.getClientId());
            Account account = findAccountById(request.getAccountId());
            User user = findUserByEmail(userEmail);

            // Müşteri-hesap ilişkisi kontrolü
            validateClientAccount(client, account);

            // Tutar kontrolü
            validateAmount(request.getAmount());

            // İşlem tarihi kontrolü ve varsayılan değer atama
            LocalDate transactionDate = request.getTransactionDate() != null
                    ? request.getTransactionDate()
                    : LocalDate.now();

            // Mevcut bakiyeyi kaydet
            BigDecimal previousBalance = account.getBalance();

            // Hesap bakiyesini güncelle
            account.setBalance(previousBalance.add(request.getAmount()));
            account.setAvailableBalance(account.getAvailableBalance().add(request.getAmount()));

            // Hesabı kaydet
            accountRepository.save(account);

            // İşlem kaydı oluştur
            Transaction transaction = Transaction.builder()
                    .client(client)
                    .account(account)
                    .user(user)
                    .transactionType(TransactionType.DEPOSIT)
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
            DepositResponse response = createDepositResponse(
                    transaction.getId(), account, client, request.getAmount(),
                    previousBalance, account.getBalance(),
                    request.getDescription(), transactionDate, transaction.getExecutedAt());

            log.info("Hesaba bakiye yükleme işlemi başarılı: Müşteri={}, Hesap={}, Tutar={}",
                    client.getFullName(), account.getAccountNumber(), request.getAmount());

            return Response.<DepositResponse>builder()
                    .statusCode(HttpStatus.OK.value())
                    .isSuccess(true)
                    .message("Bakiye yükleme işlemi başarıyla tamamlandı")
                    .data(response)
                    .build();

        } catch (Exception e) {
            log.error("Bakiye yükleme hatası: {}", e.getMessage(), e);

            return Response.<DepositResponse>builder()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .isSuccess(false)
                    .message(e.getMessage())
                    .errorCode(getErrorCode(e))
                    .build();
        }
    }

    // Yardımcı metotlar

    private Client findClientById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Müşteri bulunamadı: ID=" + clientId));
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Hesap bulunamadı: ID=" + accountId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: Email=" + email));
    }

    private void validateClientAccount(Client client, Account account) {
        if (!account.getClient().getId().equals(client.getId())) {
            throw new AccountNotFoundException("Hesap belirtilen müşteriye ait değil");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Geçersiz tutar: Tutar sıfırdan büyük olmalıdır");
        }
    }

    private DepositResponse createDepositResponse(
            Long transactionId, Account account, Client client,
            BigDecimal amount, BigDecimal previousBalance, BigDecimal newBalance,
            String description, LocalDate transactionDate, LocalDateTime executedAt) {

        return DepositResponse.builder()
                .transactionId(transactionId)
                .accountNumber(account.getAccountNumber())
                .clientName(client.getFullName())
                .currencyCode(account.getCurrency().name())
                .amount(amount)
                .previousBalance(previousBalance)
                .newBalance(newBalance)
                .description(description)
                .transactionDate(transactionDate)
                .executedAt(executedAt)
                .build();
    }

    private ErrorCode getErrorCode(Exception e) {
        if (e instanceof ClientNotFoundException) {
            return ErrorCode.CLIENT_NOT_FOUND;
        } else if (e instanceof AccountNotFoundException) {
            return ErrorCode.ACCOUNT_NOT_FOUND;
        } else if (e instanceof UserNotFoundException) {
            return ErrorCode.USER_NOT_FOUND;
        } else if (e instanceof InvalidAmountException) {
            return ErrorCode.INVALID_AMOUNT;
        } else {
            return ErrorCode.UNEXPECTED_ERROR;
        }
    }
}
