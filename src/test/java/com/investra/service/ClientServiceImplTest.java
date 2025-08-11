package com.investra.service;

import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.response.Response;
import com.investra.entity.Account;
import com.investra.entity.Client;
import com.investra.entity.User;
import com.investra.enums.Gender;
import com.investra.enums.OrderStatus;
import com.investra.exception.BusinessException;
import com.investra.exception.ErrorCode;
import com.investra.exception.UserNotFoundException;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.TradeOrderRepository;
import com.investra.repository.UserRepository;
import com.investra.service.impl.ClientServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TradeOrderRepository tradeOrderRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void createIndividualClient_ShouldReturn201_WhenClientCreated() {
        String userEmail = "user@example.com";
        CreateIndividualClientRequest request = new CreateIndividualClientRequest();
        request.setEmail("client@example.com");
        request.setFullName("Ali Veli");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.Male);
        request.setAddress("İstanbul, Türkiye");

        User user = new User();
        user.setEmail(userEmail);

        when(clientRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Response response = clientService.createClient(request, userEmail);

        assertEquals(201, response.getStatusCode());
        assertEquals("Bireysel müşteri başarıyla eklendi", response.getMessage());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    public void createIndividualClient_ShouldThrowBusinessException_WhenUserNotFound() {
        String userEmail = "user@example.com";
        CreateIndividualClientRequest request = new CreateIndividualClientRequest();
        request.setEmail("client@example.com");
        request.setFullName("Ali Veli");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.Male);
        request.setAddress("İstanbul, Türkiye");

        when(clientRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        BusinessException ex = org.junit.Assert.assertThrows(
                BusinessException.class,
                () -> clientService.createClient(request, userEmail)
        );

        assertTrue(ex.getMessage().contains(userEmail));
        assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    public void deleteClient_ShouldReturn400_WhenClientAlreadyInactive() {
        Client client = new Client();
        client.setIsActive(false);
        client.setFullName("Inactive Client");

        Response<Void> response = clientService.deleteClient(client);

        assertEquals(400, response.getStatusCode());
        assertEquals("Müşteri zaten pasif durumda.", response.getMessage());
    }

    @Test
    public void deleteClient_ShouldReturn400_WhenNegativeBalanceExists() {
        Client client = new Client();
        client.setIsActive(true);
        client.setId(1L);
        client.setFullName("Ali Veli");
        client.setEmail("ali@veli.com");

        Account account = new Account();
        account.setBalance(BigDecimal.valueOf(-10));
        account.setClient(client);

        when(accountRepository.findAllByClientId(1L))
                .thenReturn(Collections.singletonList(account));

        Response<Void> response = clientService.deleteClient(client);

        assertEquals(400, response.getStatusCode());
        assertEquals("Negatif bakiye bulunmaktadır, işleminize devam edemiyoruz.", response.getMessage());
    }

    @Test
    public void deleteClient_ShouldReturn400_WhenPendingOrdersExist() {
        Client client = new Client();
        client.setIsActive(true);
        client.setId(1L);
        client.setFullName("Müşteri 1");

        when(accountRepository.findAllByClientId(client.getId()))
                .thenReturn(Collections.singletonList(new com.investra.entity.Account() {{
                    setBalance(BigDecimal.ZERO);
                }}));

        when(tradeOrderRepository.findAllByClientId(client.getId()))
                .thenReturn(Collections.singletonList(new com.investra.entity.TradeOrder() {{
                    setStatus(com.investra.enums.OrderStatus.PENDING);
                }}));

        Response<Void> response = clientService.deleteClient(client);

        assertEquals(400, response.getStatusCode());
        assertEquals("Bekleyen emir bulundu, işleminize devam edemiyoruz.", response.getMessage());
    }

    @Test
    public void deleteClient_ShouldDeactivateClientAndSendEmail_WhenAllConditionsOk() {
        Client client = new Client();
        client.setIsActive(true);
        client.setId(1L);
        client.setEmail("client@example.com");
        client.setFullName("Active Client");

        when(accountRepository.findAllByClientId(client.getId()))
                .thenReturn(Collections.singletonList(new com.investra.entity.Account() {{
                    setBalance(BigDecimal.ZERO);
                }}));

        when(tradeOrderRepository.findAllByClientId(client.getId()))
                .thenReturn(Collections.singletonList(new com.investra.entity.TradeOrder() {{
                    setStatus(OrderStatus.EXECUTED);
                }}));

        when(emailTemplateService.processTemplate(anyString(), anyMap())).thenReturn("Email Content");

        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Response<Void> response = clientService.deleteClient(client);

        assertEquals(200, response.getStatusCode());
        assertEquals("Müşteri pasif hale getirildi.", response.getMessage());
        assertFalse(client.getIsActive());

        verify(notificationService, times(1)).sendEmail(any());
        verify(clientRepository, times(1)).save(client);
    }
}