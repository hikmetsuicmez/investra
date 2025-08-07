package com.investra.service;

import com.investra.dtos.request.AccountCreationRequest;
import com.investra.dtos.response.AccountResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.enums.AccountType;
import com.investra.enums.Currency;
import com.investra.exception.ClientNotFoundException;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.mapper.AccountMapper;
import com.investra.service.impl.AccountServiceImpl;
import com.sun.jdi.request.InvalidRequestStateException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountCreationRequest request;
    private Client client;

    @Before
    public void setUp() {
        request = new AccountCreationRequest();
        request.setClientId(1L);
        request.setIban("TR123456789012345678901234");
        request.setAccountNumberAtBroker("ACC123");
        request.setAccountType(AccountType.SETTLEMENT);
        request.setCurrency(Currency.TRY);
        request.setInitialBalance(BigDecimal.valueOf(1000));
        request.setNickname("Yatırım Hesabı");
        request.setBrokerName("Test Broker");
        request.setBrokerCode("BRK01");
        request.setCustodianName("Test Custodian");
        request.setCustodianCode("CST01");

        client = new Client();
        client.setId(1L);
        client.setFullName("Test User");
    }

    @Test
    public void createAccount_ShouldReturnSuccessResponse_WhenValidRequest() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(accountRepository.existsByIban(request.getIban())).thenReturn(false);
        when(accountRepository.existsByAccountNumberAtBroker(request.getAccountNumberAtBroker())).thenReturn(false);
        when(accountRepository.findByClientIdAndAccountTypeAndCurrency(1L, AccountType.SETTLEMENT, Currency.TRY)).thenReturn(java.util.Collections.emptyList());

        // Account nesnesi oluşturup kaydedecekmiş gibi yapıyoruz
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        when(accountRepository.save(accountCaptor.capture())).thenAnswer(i -> {
            Account acc = accountCaptor.getValue();
            acc.setId(100L); // Kaydedilmiş gibi id set ediyoruz
            return acc;
        });

        Response<AccountResponse> response = accountService.createAccount(request);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(201, response.getStatusCode());
        assertEquals("Hesap başarıyla oluşturuldu", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("TR123456789012345678901234", response.getData().getIban());

        verify(accountRepository).save(any(Account.class));
    }
    @Test
    public void getAccountsByClientId_ShouldReturnAccounts_WhenClientExists() {
        Long clientId = 1L;
        when(clientRepository.existsById(clientId)).thenReturn(true);

        Account account = new Account();
        account.setId(1L);
        account.setIban("TR123");
        account.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findByClientIdOrderByCreatedAtDesc(clientId))
                .thenReturn(java.util.Collections.singletonList(account));

        Response<List<AccountResponse>> response = accountService.getAccountsByClientId(clientId);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertEquals(1, response.getData().size());
        assertEquals("TR123", response.getData().get(0).getIban());
    }
    @Test(expected = ClientNotFoundException.class)
    public void getAccountsByClientId_ShouldThrowException_WhenClientNotFound() {
        when(clientRepository.existsById(1L)).thenReturn(false);

        accountService.getAccountsByClientId(1L);
    }
    @Test
    public void getAccountById_ShouldReturnAccount_WhenExists() {
        Long accountId = 10L;
        Account account = new Account();
        account.setId(accountId);
        account.setIban("TR987");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Response<AccountResponse> response = accountService.getAccountById(accountId);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(200, response.getStatusCode());
        assertEquals("TR987", response.getData().getIban());
    }
    @Test(expected = InvalidRequestStateException.class)
    public void getAccountById_ShouldThrowException_WhenNotFound() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        accountService.getAccountById(1L);
    }

}