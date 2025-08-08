package com.investra.service;

import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.response.Response;
import com.investra.entity.Client;
import com.investra.entity.User;
import com.investra.enums.OrderStatus;
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

        User user = new User();
        user.setEmail(userEmail);

        when(clientRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Response response = clientService.createClient(request, userEmail);

        assertEquals(201, response.getStatusCode());
        assertEquals("Bireysel müşteri başarıyla eklendi", response.getMessage());
        verify(clientRepository, times(1)).save(any(Client.class));
        System.out.println("Response status code: " + response.getStatusCode());
    }

    @Test
    public void createIndividualClient_ShouldThrowUserNotFoundException_WhenUserNotFound() {
        CreateIndividualClientRequest request = new CreateIndividualClientRequest();
        request.setEmail("client@example.com");
        String userEmail = "notfound@example.com";

        when(clientRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        try {
            clientService.createClient(request, userEmail);
            fail("UserNotFoundException bekleniyordu ama fırlatılmadı!");
        } catch (UserNotFoundException ex) {
            System.out.println("Beklenen exception fırlatıldı: " + ex.getMessage());
            assertTrue(ex.getMessage().contains(userEmail));
        }
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

        when(accountRepository.findAllByClientId(client.getId()))
                .thenReturn(Collections.singletonList(new com.investra.entity.Account() {{
                    setBalance(new BigDecimal("-10"));
                }}));

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