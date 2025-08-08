package com.investra.service.impl;


import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.response.*;
import com.investra.entity.*;
import com.investra.enums.NotificationType;
import com.investra.enums.OrderStatus;
import com.investra.exception.UserNotFoundException;
import com.investra.mapper.ClientMapper;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.TradeOrderRepository;
import com.investra.repository.UserRepository;
import com.investra.service.AbstractStockTradeService;
import com.investra.service.ClientService;
import com.investra.service.EmailTemplateService;
import com.investra.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.investra.mapper.ClientMapper.mapToEntity;
import static com.investra.mapper.ClientMapper.mapToResponse;
import static com.investra.service.helper.AdminOperationsValidator.duplicateResourceCheck;

@Service
@Slf4j
public class ClientServiceImpl extends AbstractStockTradeService implements ClientService {
    private final UserRepository userRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final AccountRepository accountRepository;
    private final EmailTemplateService emailTemplateService;
    private final NotificationService notificationService;

    public ClientServiceImpl(
            ClientRepository clientRepository,
            UserRepository userRepository,
            TradeOrderRepository tradeOrderRepository,
            AccountRepository accountRepository,
            EmailTemplateService emailTemplateService,
            NotificationService notificationService
    ) {
        super(clientRepository);
        this.userRepository = userRepository;
        this.tradeOrderRepository = tradeOrderRepository;
        this.accountRepository = accountRepository;
        this.emailTemplateService = emailTemplateService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Response<CreateClientResponse> createClient(CreateClientRequest request, String userEmail) {
        try {
            log.info("Müşteri oluşturma işlemi başlatıldı. Kullanıcı: {}, Müşteri Tipi: {}",
                    userEmail, request.getClass().getSimpleName());

            if (request instanceof CreateIndividualClientRequest individualRequest) {
                return createIndividualClient(individualRequest, userEmail);
            } else if (request instanceof CreateCorporateClientRequest corporateRequest) {
                return createCorporateClient(corporateRequest, userEmail);
            } else {
                log.warn("Geçersiz müşteri tipi ile oluşturma denemesi yapıldı. Tip: {}", request.getClass().getName());

                return Response.<CreateClientResponse>builder()
                        .statusCode(400)
                        .message("Geçersiz müşteri tipi")
                        .build();
            }
        } catch (IllegalArgumentException e) {
            log.error("Müşteri oluşturma hatası (geçersiz argüman): {}", e.getMessage(), e);
            return Response.<CreateClientResponse>builder()
                    .statusCode(400)
                    .message(e.getMessage())
                    .build();
        }
    }

    private Response<CreateClientResponse> createIndividualClient(CreateIndividualClientRequest request, String userEmail) {

        if (request.getEmail() != null) {
            duplicateResourceCheck(() -> clientRepository.findByEmail(request.getEmail()).isPresent(), "Bu email ile kayıtlı bir müşteri mevcut");
        }
        if (request.getNationalityNumber() != null && !request.getNationalityNumber().isBlank()) {
            duplicateResourceCheck(() -> clientRepository.findByNationalityNumber(request.getNationalityNumber()).isPresent(),
                    "Bu TCKN ile kayıtlı bir müşteri mevcut");
        }
        if (request.getBlueCardNo() != null && !request.getBlueCardNo().isBlank()) {
            duplicateResourceCheck(() -> clientRepository.findByBlueCardNo(request.getBlueCardNo()).isPresent(),
                    "Bu Mavi Kart ile kayıtlı bir müşteri mevcut");
        }
        if (request.getPassportNo() != null && !request.getPassportNo().isBlank()) {
            duplicateResourceCheck(() -> clientRepository.findByPassportNo(request.getPassportNo()).isPresent(),
                    "Bu Pasaport numarası ile kayıtlı bir müşteri mevcut");
        }
        if (request.getTaxId() != null) {
            duplicateResourceCheck(() -> clientRepository.findByTaxId(request.getTaxId()).isPresent(), "Bu vergi numarası ile kayıtlı bir müşteri mevcut");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userEmail));

        Client client = mapToEntity(request, user);
        client.setIsActive(true);
        client.setCreatedAt(LocalDateTime.now());
        clientRepository.save(client);
        log.info("Bireysel müşteri oluşturuldu. ID: {}, Email: {}", client.getId(), client.getEmail());

        CreateClientResponse response = mapToResponse(request);

        return Response.<CreateClientResponse>builder()
                .statusCode(201)
                .message("Bireysel müşteri başarıyla eklendi")
                .data(response)
                .build();
    }

    private Response<CreateClientResponse> createCorporateClient(CreateCorporateClientRequest request, String userEmail) {
        if (request.getEmail() != null) {
            duplicateResourceCheck(() -> clientRepository.findByEmail(request.getEmail()).isPresent(), "Bu email ile kayıtlı bir müşteri mevcut");
        }
        duplicateResourceCheck(() -> clientRepository.findByTaxNumber(request.getTaxNumber()).isPresent(), "Bu vergi numarası ile kayıtlı bir müşteri mevcut");

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                        log.warn("Kullanıcı bulunamadı: id={} ",userEmail);
                        return new UserNotFoundException("Kullanıcı bulunamadı: id= " + userEmail);
                        });
        Client client = mapToEntity(request, user);
        client.setIsActive(true);
        client.setCreatedAt(LocalDateTime.now());
        clientRepository.save(client);
        log.info("Kurumsal müşteri oluşturuldu. ID: {}, Vergi No: {}", client.getId(), request.getTaxNumber());

        CreateClientResponse response = mapToResponse(request);

        return Response.<CreateClientResponse>builder()
                .statusCode(201)
                .message("Kurumsal müşteri başarıyla eklendi")
                .data(response)
                .build();
    }

    @Override
    public Client findEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Müşteri bulunamadı: id={}", id);
                    return new EntityNotFoundException("Müşteri bulunamadı: id=" + id);
                });
    }


    @Override
    @Cacheable(value = "clients", key = "'active_clients'")
    public Response<List<ClientDTO>> getActiveClients() {
        List<Client> activeClients = clientRepository.findAll().stream()
                .filter(client -> Boolean.TRUE.equals(client.getIsActive()))
                .toList();

        if (activeClients.isEmpty()) {
            log.info("getActiveClients: Aktif müşteri bulunamadı.");
            return Response.<List<ClientDTO>>builder()
                    .statusCode(404)
                    .message("Aktif müşteri bulunamadı")
                    .data(List.of())
                    .build();
        }

        List<ClientDTO> clientResponse = activeClients.stream()
                .map(ClientMapper::toClientDTO)
                .toList();

        log.info("getActiveClients: Aktif müşteriler getirildi.");
        return Response.<List<ClientDTO>>builder()
                .statusCode(200)
                .message("Aktif müşteriler getirildi")
                .data(clientResponse)
                .build();
    }

    @Override
    @Cacheable(value = "clients", key = "'inactive_clients'")
    public Response<List<ClientDTO>> getInactiveClients() {
        List<Client> inactiveClients = clientRepository.findAll().stream()
                .filter(client -> Boolean.FALSE.equals(client.getIsActive()))
                .toList();

        if (inactiveClients.isEmpty()) {
            log.info("getInactiveClients: Pasif müşteri bulunamadı.");
            return Response.<List<ClientDTO>>builder()
                    .statusCode(404)
                    .message("Pasif müşteri bulunamadı")
                    .data(List.of())
                    .build();
        }

        List<ClientDTO> clientResponse = inactiveClients.stream()
                .map(ClientMapper::toClientDTO)
                .toList();

        log.info("getInactiveClients: Pasif müşteriler getirildi.");
        return Response.<List<ClientDTO>>builder()
                .statusCode(200)
                .message("Pasif müşteriler getirildi")
                .data(clientResponse)
                .build();
    }

    @Override
    @CacheEvict(value = "clients", allEntries = true)
    public Response<Void> deleteClient(Client client) {
        LocalDateTime requestTimestamp = LocalDateTime.now();
        log.info("Delete client isteği alındı. Zaman: {}, Müşteri:{}", requestTimestamp, client.getFullName());

        try {
            if (!client.getIsActive()) {
                return Response.<Void>builder()
                        .statusCode(400)
                        .message("Müşteri zaten pasif durumda.")
                        .build();
            }

            //negatif bakiye kontrolü
            List<Account> accounts = accountRepository.findAllByClientId(client.getId());
            boolean hasNegativeBalance = accounts.stream()
                    .anyMatch(account -> account.getBalance().compareTo(BigDecimal.ZERO) < 0);

            if (hasNegativeBalance) {
                return Response.<Void>builder()
                        .statusCode(400)
                        .message("Negatif bakiye bulunmaktadır, işleminize devam edemiyoruz.")
                        .build();
            }

            //bekleyen emir kontrolü
            List<TradeOrder> orders = tradeOrderRepository.findAllByClientId(client.getId());
            boolean hasPendingOrders = orders.stream()
                    .anyMatch(order -> order.getStatus() == OrderStatus.PENDING);

            if (hasPendingOrders) {
                return Response.<Void>builder()
                        .statusCode(400)
                        .message("Bekleyen emir bulundu, işleminize devam edemiyoruz.")
                        .build();
            }
            client.setIsActive(false);
            clientRepository.save(client);

            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("userName", client.getFullName());
            templateVariables.put("deactivationMessage", "Hesabınız talebiniz doğrultusunda inaktif hale getirilmiştir.");
            String emailContent = emailTemplateService.processTemplate("client-deactivation-info", templateVariables);

            NotificationDTO notificationDTO = NotificationDTO.builder()
                    .recipient(client.getEmail())
                    .subject("Investra Hesabınız İnaktif Hale Getirildi")
                    .content(emailContent)
                    .type(NotificationType.INFO)
                    .isHtml(true)
                    .build();

            try {
                notificationService.sendEmail(notificationDTO);
                log.info("Müşteriye bilgilendirme e-postası gönderildi: {}", client.getEmail());
            } catch (Exception e) {
                log.error("Bilgilendirme e-postası gönderilemedi. Hata: {}", e.getMessage(), e);
            }
            return Response.<Void>builder()
                    .statusCode(200)
                    .message("Müşteri pasif hale getirildi.")
                    .build();

        } catch (Exception e) {
            log.error("Hata oluştu. Client: {}, Hata: {}", client.getId(), e.getMessage(), e);
            return Response.<Void>builder()
                    .statusCode(500)
                    .message("Beklenmeyen bir hata oluştu.")
                    .build();
        }
    }

}
