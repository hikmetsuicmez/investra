package com.investra.service;

import com.investra.dtos.request.DepositRequest;
import com.investra.dtos.response.DepositResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.Transaction;
import com.investra.entity.User;
import com.investra.enums.Currency;
import com.investra.enums.TransactionStatus;
import com.investra.enums.TransactionType;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.TransactionRepository;
import com.investra.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountDepositServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountDepositServiceImpl accountDepositService;

    private DepositRequest depositRequest;
    private Account account;
    private Client client;
    private User user;

    @Before
    public void setUp() {
        depositRequest = new DepositRequest();
        depositRequest.setClientId(1L);
        depositRequest.setAccountId(2L);
        depositRequest.setAmount(new BigDecimal("1000.00"));
        depositRequest.setDescription("Deposit test");
        depositRequest.setTransactionDate(LocalDate.of(2025, 8, 7));

        client = new Client();
        client.setId(1L);
        client.setFullName("Test Client");

        account = new Account();
        account.setId(2L);
        account.setClient(client);
        account.setAccountNumber("TR123");
        account.setBalance(new BigDecimal("5000.00"));
        account.setAvailableBalance(new BigDecimal("5000.00"));
        account.setCurrency(Currency.TRY);

        user = new User();
        user.setEmail("admin@example.com");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(account));
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));

        Transaction savedTransaction = Transaction.builder()
                .id(100L)
                .transactionType(TransactionType.DEPOSIT)
                .transactionDate(depositRequest.getTransactionDate())
                .executedAt(LocalDateTime.now())
                .client(client)
                .account(account)
                .amount(depositRequest.getAmount())
                .previousBalance(account.getBalance())
                .newBalance(account.getBalance().add(depositRequest.getAmount()))
                .description(depositRequest.getDescription())
                .status(TransactionStatus.COMPLETED)
                .user(user)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);
    }

    @Test
    public void depositToAccount_ShouldReturnSuccess_WhenValidRequest() {
        Response<DepositResponse> response = accountDepositService.depositToAccount(depositRequest, "admin@example.com");

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertEquals("Bakiye yükleme işlemi başarıyla tamamlandı", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("TR123", response.getData().getAccountNumber());
        assertEquals(new BigDecimal("6000.00"), response.getData().getNewBalance());

        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }
    @Test
    public void depositToAccount_ShouldReturnClientNotFound_WhenClientDoesNotExist() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        Response<DepositResponse> response = accountDepositService.depositToAccount(depositRequest, "admin@example.com");

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("Müşteri bulunamadı: ID=1", response.getMessage());
        assertEquals("CLIENT_NOT_FOUND", response.getErrorCode().name());
    }

    @Test
    public void depositToAccount_ShouldReturnAccountNotFound_WhenAccountDoesNotExist() {
        when(accountRepository.findById(2L)).thenReturn(Optional.empty());

        Response<DepositResponse> response = accountDepositService.depositToAccount(depositRequest, "admin@example.com");

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("Hesap bulunamadı: ID=2", response.getMessage());
        assertEquals("ACCOUNT_NOT_FOUND", response.getErrorCode().name());
    }
    @Test
    public void depositToAccount_ShouldReturnInvalidAmount_WhenAmountIsNull() {
        depositRequest.setAmount(null);

        Response<DepositResponse> response = accountDepositService.depositToAccount(depositRequest, "admin@example.com");

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("Geçersiz tutar: Tutar sıfırdan büyük olmalıdır", response.getMessage());
        assertEquals("INVALID_AMOUNT", response.getErrorCode().name());
    }
    @Test
    public void depositToAccount_ShouldReturnUserNotFound_WhenUserDoesNotExist() {
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        Response<DepositResponse> response = accountDepositService.depositToAccount(depositRequest, "admin@example.com");

        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(400, response.getStatusCode());
        assertEquals("Kullanıcı bulunamadı: Email=admin@example.com", response.getMessage());
        assertEquals("USER_NOT_FOUND", response.getErrorCode().name());
    }
}