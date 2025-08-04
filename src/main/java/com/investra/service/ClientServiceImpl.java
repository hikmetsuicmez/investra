package com.investra.service;


import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.User;
import com.investra.exception.UserNotFoundException;
import com.investra.mapper.ClientMapper;
import com.investra.repository.ClientRepository;
import com.investra.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.investra.entity.Client;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.investra.mapper.ClientMapper.mapToEntity;
import static com.investra.mapper.ClientMapper.mapToResponse;
import static com.investra.utils.AdminOperationsValidator.duplicateResourceCheck;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;


    @Transactional
    public Response<CreateClientResponse> createClient(CreateClientRequest request, String userEmail) {
        try {
            if (request instanceof CreateIndividualClientRequest individualRequest) {
                return createIndividualClient(individualRequest, userEmail);
            } else if (request instanceof CreateCorporateClientRequest corporateRequest) {
                return createCorporateClient(corporateRequest, userEmail);
            } else {
                return Response.<CreateClientResponse>builder()
                        .statusCode(400)
                        .message("Geçersiz müşteri tipi")
                        .build();
            }
        } catch (IllegalArgumentException e) {
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
                .orElseThrow(() -> new UserNotFoundException("Kullanıcı bulunamadı: " + userEmail));

        Client client = mapToEntity(request, user);
        client.setIsActive(true);
        client.setCreatedAt(LocalDateTime.now());
        clientRepository.save(client);

        CreateClientResponse response = mapToResponse(request);

        return Response.<CreateClientResponse>builder()
                .statusCode(201)
                .message("Kurumsal müşteri başarıyla eklendi")
                .data(response)
                .build();
    }


    @Override
    public Optional<ClientSearchResponse> findClientByAnyIdentifier(
            String nationalityNumber,
            String passportNo,
            String blueCardNo,
            String taxNumber,
            String registrationNumber
    ) {
        Optional<Client> clientOpt = clientRepository.findFirstByNationalityNumberOrPassportNoOrBlueCardNoOrTaxNumberOrRegistrationNumber(
                nationalityNumber, passportNo, blueCardNo, taxNumber, registrationNumber
        );

        return clientOpt.map(ClientMapper::mapToClientSearchResponse);
    }


}
