package com.investra.service.impl;

import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.request.UpdateClientRequest;
import com.investra.dtos.request.UpdateCorporateClientRequest;
import com.investra.dtos.request.UpdateIndividualClientRequest;
import com.investra.dtos.response.*;
import com.investra.entity.*;
import com.investra.enums.NotificationType;
import com.investra.enums.OrderStatus;
import com.investra.exception.BusinessException;
import com.investra.exception.ErrorCode;
import com.investra.exception.UserNotFoundException;
import com.investra.mapper.ClientMapper;
import com.investra.repository.AccountRepository;
import com.investra.repository.ClientRepository;
import com.investra.repository.TradeOrderRepository;
import com.investra.repository.UserRepository;
import com.investra.service.helper.ExceptionUtil;
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
import java.security.SecureRandom;
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
                        NotificationService notificationService) {
                super(clientRepository);
                this.userRepository = userRepository;
                this.tradeOrderRepository = tradeOrderRepository;
                this.accountRepository = accountRepository;
                this.emailTemplateService = emailTemplateService;
                this.notificationService = notificationService;
        }

        @Transactional
        // @CacheEvict(value = "clients", allEntries = true)
        public Response<CreateClientResponse> createClient(CreateClientRequest request, String userEmail) {
                try {
                        log.info("Müşteri oluşturma işlemi başlatıldı. Kullanıcı: {}, Müşteri Tipi: {}",
                                        userEmail, request.getClass().getSimpleName());

                        if (request instanceof CreateIndividualClientRequest individualRequest) {
                                return createIndividualClient(individualRequest, userEmail);
                        } else if (request instanceof CreateCorporateClientRequest corporateRequest) {
                                return createCorporateClient(corporateRequest, userEmail);
                        } else {
                                log.warn("Geçersiz müşteri tipi ile oluşturma denemesi yapıldı. Tip: {}",
                                                request.getClass().getName());

                                return Response.<CreateClientResponse>builder()
                                                .statusCode(400)
                                                .message("Geçersiz müşteri tipi. INDIVIDUAL veya CORPORATE olmalıdır")
                                                .build();
                        }
                } catch (BusinessException e) {
                        log.warn("Müşteri oluşturma iş kuralı hatası: {}", e.getMessage());
                        return Response.<CreateClientResponse>builder()
                                        .statusCode(400)
                                        .message(e.getMessage())
                                        .errorCode(e.getErrorCode())
                                        .build();
                } catch (IllegalArgumentException e) {
                        log.warn("Müşteri oluşturma validasyon hatası: {}", e.getMessage());
                        return Response.<CreateClientResponse>builder()
                                        .statusCode(400)
                                        .message(e.getMessage())
                                        .errorCode(ErrorCode.VALIDATION_ERROR)
                                        .build();
                } catch (Exception e) {
                        log.error("Müşteri oluşturma beklenmeyen hatası: {}", e.getMessage(), e);
                        return Response.<CreateClientResponse>builder()
                                        .statusCode(500)
                                        .message("Müşteri oluşturulurken beklenmeyen bir hata oluştu")
                                        .errorCode(ExceptionUtil.getErrorCode(e))
                                        .build();
                }
        }

        @Override
        @Transactional
        //@CacheEvict(value = "clients", allEntries = true)
        public Response<UpdateClientResponse> updateClient(Long clientId, UpdateClientRequest request,
                        String userEmail) {
                try {
                        log.info("Müşteri güncelleme işlemi başlatıldı. Kullanıcı: {}, Müşteri ID: {}, Müşteri Tipi: {}",
                                        userEmail, clientId, request.getClass().getSimpleName());

                        // Müşteriyi bul
                        Client existingClient = findEntityById(clientId);

                        if (!existingClient.getIsActive()) {
                                return Response.<UpdateClientResponse>builder()
                                                .statusCode(400)
                                                .message("Pasif müşteri güncellenemez")
                                                .build();
                        }

                        if (request instanceof UpdateIndividualClientRequest individualRequest) {
                                return updateIndividualClient(existingClient, individualRequest, userEmail);
                        } else if (request instanceof UpdateCorporateClientRequest corporateRequest) {
                                return updateCorporateClient(existingClient, corporateRequest, userEmail);
                        } else {
                                log.warn("Geçersiz müşteri tipi ile güncelleme denemesi yapıldı. Tip: {}",
                                                request.getClass().getName());
                                return Response.<UpdateClientResponse>builder()
                                                .statusCode(400)
                                                .message("Geçersiz müşteri tipi")
                                                .build();
                        }
                } catch (BusinessException e) {
                        log.warn("Müşteri güncelleme iş kuralı hatası: {}", e.getMessage());
                        return Response.<UpdateClientResponse>builder()
                                        .statusCode(400)
                                        .message(e.getMessage())
                                        .errorCode(e.getErrorCode())
                                        .build();
                } catch (IllegalArgumentException e) {
                        log.warn("Müşteri güncelleme validasyon hatası: {}", e.getMessage());
                        return Response.<UpdateClientResponse>builder()
                                        .statusCode(400)
                                        .message(e.getMessage())
                                        .errorCode(ErrorCode.VALIDATION_ERROR)
                                        .build();
                } catch (Exception e) {
                        log.error("Müşteri güncelleme beklenmeyen hatası: {}", e.getMessage(), e);
                        return Response.<UpdateClientResponse>builder()
                                        .statusCode(500)
                                        .message("Müşteri güncellenirken beklenmeyen bir hata oluştu")
                                        .errorCode(ExceptionUtil.getErrorCode(e))
                                        .build();
                }
        }

        private Response<CreateClientResponse> createIndividualClient(CreateIndividualClientRequest request,
                        String userEmail) {
                // Zorunlu alanların kontrolü
                if (request.getFullName() == null || request.getFullName().isBlank()) {
                        throw new BusinessException("Müşteri adı boş olamaz", ErrorCode.VALIDATION_ERROR);
                }

                // Mükerrer kayıt kontrolü
                if (request.getEmail() != null) {
                        duplicateResourceCheck(() -> clientRepository.findByEmail(request.getEmail()).isPresent(),
                                        "Bu email ile kayıtlı bir müşteri mevcut", ErrorCode.OPERATION_FAILED);
                }
                if (request.getNationalityNumber() != null && !request.getNationalityNumber().isBlank()) {
                        duplicateResourceCheck(
                                        () -> clientRepository.findByNationalityNumber(request.getNationalityNumber())
                                                        .isPresent(),
                                        "Bu TCKN ile kayıtlı bir müşteri mevcut", ErrorCode.OPERATION_FAILED);
                }
                if (request.getBlueCardNo() != null && !request.getBlueCardNo().isBlank()) {
                        duplicateResourceCheck(
                                        () -> clientRepository.findByBlueCardNo(request.getBlueCardNo()).isPresent(),
                                        "Bu Mavi Kart ile kayıtlı bir müşteri mevcut", ErrorCode.OPERATION_FAILED);
                }
                if (request.getPassportNo() != null && !request.getPassportNo().isBlank()) {
                        duplicateResourceCheck(
                                        () -> clientRepository.findByPassportNo(request.getPassportNo()).isPresent(),
                                        "Bu Pasaport numarası ile kayıtlı bir müşteri mevcut",
                                        ErrorCode.OPERATION_FAILED);
                }
                if (request.getTaxId() != null) {
                        duplicateResourceCheck(() -> clientRepository.findByTaxId(request.getTaxId()).isPresent(),
                                        "Bu vergi numarası ile kayıtlı bir müşteri mevcut", ErrorCode.OPERATION_FAILED);
                }

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(
                                                () -> new BusinessException("Kullanıcı bulunamadı: " + userEmail,
                                                                ErrorCode.USER_NOT_FOUND));

                Client client = mapToEntity(request, user);
                String clientNumber = generateRandomClientNumber();
                client.setClientNumber(clientNumber);
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

        private Response<CreateClientResponse> createCorporateClient(CreateCorporateClientRequest request,
                        String userEmail) {
                if (request.getEmail() != null) {
                        duplicateResourceCheck(() -> clientRepository.findByEmail(request.getEmail()).isPresent(),
                                        "Bu email ile kayıtlı bir müşteri mevcut", ErrorCode.OPERATION_FAILED);
                }
                duplicateResourceCheck(() -> clientRepository.findByTaxNumber(request.getTaxNumber()).isPresent(),
                                "Bu vergi numarası ile kayıtlı bir müşteri mevcut", ErrorCode.OPERATION_FAILED);

                User user = userRepository.findByEmail(userEmail)
                                .orElseThrow(() -> {
                                        log.warn("Kullanıcı bulunamadı: id={} ", userEmail);
                                        return new UserNotFoundException("Kullanıcı bulunamadı: id= " + userEmail);
                                });
                Client client = mapToEntity(request, user);
                String clientNumber = generateRandomClientNumber();
                client.setClientNumber(clientNumber);
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
        // @Cacheable(value = "clients", key = "'active_clients'")
        public Response<List<ClientDTO>> getActiveClients() {
                log.info("getActiveClients: Tüm müşteriler sorgulanıyor...");
                List<Client> allClients = clientRepository.findAll();
                log.info("getActiveClients: Toplam {} müşteri bulundu", allClients.size());

                List<Client> activeClients = allClients.stream()
                                .filter(client -> Boolean.TRUE.equals(client.getIsActive()))
                                .toList();
                log.info("getActiveClients: {} aktif müşteri bulundu", activeClients.size());

                // Debug: Her müşterinin durumunu logla
                allClients.forEach(client -> {
                        log.debug("getActiveClients: Müşteri ID: {}, İsim: {}, Aktif: {}",
                                        client.getId(), client.getFullName(), client.getIsActive());
                });

                if (activeClients.isEmpty()) {
                        log.info("getActiveClients: Aktif müşteri bulunamadı. Tüm müşteriler pasif durumda.");
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
        // @Cacheable(value = "clients", key = "'inactive_clients'")
        public Response<List<ClientDTO>> getInactiveClients() {
                log.info("getInactiveClients: Tüm müşteriler sorgulanıyor...");
                List<Client> allClients = clientRepository.findAll();
                log.info("getInactiveClients: Toplam {} müşteri bulundu", allClients.size());

                List<Client> inactiveClients = allClients.stream()
                                .filter(client -> Boolean.FALSE.equals(client.getIsActive()))
                                .toList();
                log.info("getInactiveClients: {} pasif müşteri bulundu", inactiveClients.size());

                // Debug: Her müşterinin durumunu logla
                allClients.forEach(client -> {
                        log.debug("getInactiveClients: Müşteri ID: {}, İsim: {}, Aktif: {}",
                                        client.getId(), client.getFullName(), client.getIsActive());
                });

                if (inactiveClients.isEmpty()) {
                        log.info("getInactiveClients: Pasif müşteri bulunamadı. Tüm müşteriler aktif durumda.");
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
        //@CacheEvict(value = "clients", allEntries = true)
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

                        // negatif bakiye kontrolü
                        List<Account> accounts = accountRepository.findAllByClientId(client.getId());
                        boolean hasNegativeBalance = accounts.stream()
                                        .anyMatch(account -> account.getBalance().compareTo(BigDecimal.ZERO) < 0);

                        if (hasNegativeBalance) {
                                return Response.<Void>builder()
                                                .statusCode(400)
                                                .message("Negatif bakiye bulunmaktadır, işleminize devam edemiyoruz.")
                                                .build();
                        }

                        // bekleyen emir kontrolü
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
                        templateVariables.put("deactivationMessage",
                                        "Hesabınız talebiniz doğrultusunda inaktif hale getirilmiştir.");
                        String emailContent = emailTemplateService.processTemplate("client-deactivation-info",
                                        templateVariables);

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
                                return Response.<Void>builder()
                                                .statusCode(500)
                                                .message("E-posta gönderilirken hata oluştu.")
                                                .errorCode(ExceptionUtil.getErrorCode(e))
                                                .build();
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

        private Response<UpdateClientResponse> updateIndividualClient(Client existingClient,
                        UpdateIndividualClientRequest request, String userEmail) {
                try {
                        // Mükerrer kayıt kontrolü (kendisi hariç)
                        if (request.getEmail() != null && !request.getEmail().equals(existingClient.getEmail())) {
                                duplicateResourceCheck(
                                                () -> clientRepository.findByEmail(request.getEmail()).isPresent(),
                                                "Bu email ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }
                        if (request.getNationalityNumber() != null && !request.getNationalityNumber().isBlank()
                                        && !request.getNationalityNumber()
                                                        .equals(existingClient.getNationalityNumber())) {
                                duplicateResourceCheck(
                                                () -> clientRepository
                                                                .findByNationalityNumber(request.getNationalityNumber())
                                                                .isPresent(),
                                                "Bu TCKN ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }
                        if (request.getBlueCardNo() != null && !request.getBlueCardNo().isBlank()
                                        && !request.getBlueCardNo().equals(existingClient.getBlueCardNo())) {
                                duplicateResourceCheck(
                                                () -> clientRepository.findByBlueCardNo(request.getBlueCardNo())
                                                                .isPresent(),
                                                "Bu Mavi Kart ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }
                        if (request.getPassportNo() != null && !request.getPassportNo().isBlank()
                                        && !request.getPassportNo().equals(existingClient.getPassportNo())) {
                                duplicateResourceCheck(
                                                () -> clientRepository.findByPassportNo(request.getPassportNo())
                                                                .isPresent(),
                                                "Bu Pasaport numarası ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }
                        if (request.getTaxId() != null && !request.getTaxId().equals(existingClient.getTaxId())) {
                                duplicateResourceCheck(
                                                () -> clientRepository.findByTaxId(request.getTaxId()).isPresent(),
                                                "Bu vergi numarası ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }

                        // Müşteri bilgilerini güncelle
                        updateCommonFields(existingClient, request);

                        if (request.getFullName() != null) {
                                existingClient.setFullName(request.getFullName());
                        }
                        if (request.getNationalityType() != null) {
                                existingClient.setNationalityType(request.getNationalityType());
                        }
                        if (request.getBirthDate() != null) {
                                existingClient.setBirthDate(request.getBirthDate());
                        }
                        if (request.getGender() != null) {
                                existingClient.setGender(request.getGender());
                        }
                        if (request.getTaxId() != null) {
                                existingClient.setTaxId(request.getTaxId());
                        }
                        if (request.getPassportNo() != null) {
                                existingClient.setPassportNo(request.getPassportNo());
                        }
                        if (request.getBlueCardNo() != null) {
                                existingClient.setBlueCardNo(request.getBlueCardNo());
                        }
                        if (request.getNationalityNumber() != null) {
                                existingClient.setNationalityNumber(request.getNationalityNumber());
                        }
                        if (request.getProfession() != null) {
                                existingClient.setProfession(request.getProfession());
                        }
                        if (request.getEducationStatus() != null) {
                                existingClient.setEducationStatus(request.getEducationStatus());
                        }
                        if (request.getMonthlyIncome() != null) {
                                existingClient.setMonthlyIncome(request.getMonthlyIncome());
                        }
                        if (request.getEstimatedTransactionVolume() != null) {
                                existingClient.setEstimatedTransactionVolume(request.getEstimatedTransactionVolume());
                        }

                        existingClient = clientRepository.save(existingClient);
                        log.info("Bireysel müşteri güncellendi. ID: {}, Email: {}", existingClient.getId(),
                                        existingClient.getEmail());

                        UpdateClientResponse response = buildUpdateResponse(existingClient);
                        return Response.<UpdateClientResponse>builder()
                                        .statusCode(200)
                                        .message("Bireysel müşteri başarıyla güncellendi")
                                        .data(response)
                                        .build();

                } catch (Exception e) {
                        log.error("Bireysel müşteri güncelleme hatası: {}", e.getMessage(), e);
                        throw e;
                }
        }

        private Response<UpdateClientResponse> updateCorporateClient(Client existingClient,
                        UpdateCorporateClientRequest request, String userEmail) {
                try {
                        // Mükerrer kayıt kontrolü (kendisi hariç)
                        if (request.getEmail() != null && !request.getEmail().equals(existingClient.getEmail())) {
                                duplicateResourceCheck(
                                                () -> clientRepository.findByEmail(request.getEmail()).isPresent(),
                                                "Bu email ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }
                        if (request.getTaxNumber() != null
                                        && !request.getTaxNumber().equals(existingClient.getTaxNumber())) {
                                duplicateResourceCheck(
                                                () -> clientRepository.findByTaxNumber(request.getTaxNumber())
                                                                .isPresent(),
                                                "Bu vergi numarası ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }
                        if (request.getRegistrationNumber() != null && !request.getRegistrationNumber()
                                        .equals(existingClient.getRegistrationNumber())) {
                                duplicateResourceCheck(
                                                () -> clientRepository
                                                                .findByRegistrationNumber(
                                                                                request.getRegistrationNumber())
                                                                .isPresent(),
                                                "Bu sicil numarası ile kayıtlı başka bir müşteri mevcut",
                                                ErrorCode.OPERATION_FAILED);
                        }

                        // Müşteri bilgilerini güncelle
                        updateCommonFields(existingClient, request);

                        if (request.getCompanyName() != null) {
                                existingClient.setCompanyName(request.getCompanyName());
                        }
                        if (request.getTaxNumber() != null) {
                                existingClient.setTaxNumber(request.getTaxNumber());
                        }
                        if (request.getRegistrationNumber() != null) {
                                existingClient.setRegistrationNumber(request.getRegistrationNumber());
                        }
                        if (request.getCompanyType() != null) {
                                existingClient.setCompanyType(request.getCompanyType());
                        }
                        if (request.getSector() != null) {
                                existingClient.setSector(request.getSector());
                        }
                        if (request.getMonthlyRevenue() != null) {
                                existingClient.setMonthlyRevenue(request.getMonthlyRevenue());
                        }

                        existingClient = clientRepository.save(existingClient);
                        log.info("Kurumsal müşteri güncellendi. ID: {}, Vergi No: {}", existingClient.getId(),
                                        existingClient.getTaxNumber());

                        UpdateClientResponse response = buildUpdateResponse(existingClient);
                        return Response.<UpdateClientResponse>builder()
                                        .statusCode(200)
                                        .message("Kurumsal müşteri başarıyla güncellendi")
                                        .data(response)
                                        .build();

                } catch (Exception e) {
                        log.error("Kurumsal müşteri güncelleme hatası: {}", e.getMessage(), e);
                        throw e;
                }
        }

        private void updateCommonFields(Client client, UpdateClientRequest request) {
                if (request.getEmail() != null) {
                        client.setEmail(request.getEmail());
                }
                if (request.getPhone() != null) {
                        client.setPhone(request.getPhone());
                }
                if (request.getAddress() != null) {
                        client.setAddress(request.getAddress());
                }
                if (request.getNotes() != null) {
                        client.setNotes(request.getNotes());
                }
                if (request.getStatus() != null) {
                        client.setStatus(request.getStatus());
                }
                if (request.getIsActive() != null) {
                        client.setIsActive(request.getIsActive());
                }
        }

        private UpdateClientResponse buildUpdateResponse(Client client) {
                return ClientMapper.mapToUpdateResponse(client);
        }

        private String generateRandomClientNumber() {
                SecureRandom random = new SecureRandom();
                int number = 100000 + random.nextInt(900000); // 100000 - 999999 arası 6 haneli sayı üretecek
                return String.valueOf(number);
        }
}
