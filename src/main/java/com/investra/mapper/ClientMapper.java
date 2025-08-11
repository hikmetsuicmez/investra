package com.investra.mapper;

import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateClientRequest;
import com.investra.dtos.request.UpdateCorporateClientRequest;
import com.investra.dtos.request.UpdateIndividualClientRequest;
import com.investra.dtos.response.ClientDTO;
import com.investra.dtos.response.ClientSearchResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.UpdateClientResponse;
import com.investra.entity.Client;
import com.investra.entity.User;
import com.investra.enums.ClientType;

public class ClientMapper {

    public static Client mapToEntity(CreateClientRequest request, User user) {
        Client.ClientBuilder builder = Client.builder()
                .clientType(request.getClientType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .taxType(request.getTaxType())
                .notes(request.getNotes())
                .status(request.getStatus())
                .isActive(request.getIsActive())
                .createdAt(LocalDateTime.now())
                .user(user);

        if (request instanceof CreateIndividualClientRequest individual) {
            builder
                    .fullName(individual.getFullName())
                    .nationalityType(individual.getNationalityType())
                    .taxId(individual.getTaxId())
                    .passportNo(individual.getPassportNo())
                    .blueCardNo(individual.getBlueCardNo())
                    .nationalityNumber(individual.getNationalityNumber())
                    .birthDate(individual.getBirthDate())
                    .profession(individual.getProfession())
                    .gender(individual.getGender())
                    .educationStatus(individual.getEducationStatus())
                    .monthlyIncome(individual.getMonthlyIncome())
                    .estimatedTransactionVolume(individual.getEstimatedTransactionVolume());

        } else if (request instanceof CreateCorporateClientRequest corporate) {
            builder
                    .companyName(corporate.getCompanyName())
                    .taxNumber(corporate.getTaxNumber())
                    .registrationNumber(corporate.getRegistrationNumber())
                    .companyType(corporate.getCompanyType())
                    .sector(corporate.getSector())
                    .monthlyRevenue(corporate.getMonthlyRevenue());
        }

        return builder.build();
    }

    public static CreateClientResponse mapToResponse(CreateClientRequest request) {
        CreateClientResponse.CreateClientResponseBuilder builder = CreateClientResponse.builder()
                .clientType(request.getClientType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .taxType(request.getTaxType())
                .address(request.getAddress())
                .notes(request.getNotes())
                .status(request.getStatus())
                .isActive(request.getIsActive());

        if (request instanceof CreateIndividualClientRequest individual) {
            builder
                    .fullName(individual.getFullName())
                    .nationalityType(individual.getNationalityType())
                    .taxId(individual.getTaxId())
                    .passportNo(individual.getPassportNo())
                    .blueCardNo(individual.getBlueCardNo())
                    .nationalityNumber(individual.getNationalityNumber())
                    .birthDate(individual.getBirthDate())
                    .profession(individual.getProfession())
                    .gender(individual.getGender())
                    .educationStatus(individual.getEducationStatus())
                    .monthlyIncome(individual.getMonthlyIncome())
                    .estimatedTransactionVolume(individual.getEstimatedTransactionVolume());

        } else if (request instanceof CreateCorporateClientRequest corporate) {
            builder
                    .companyName(corporate.getCompanyName())
                    .taxNumber(corporate.getTaxNumber())
                    .registrationNumber(corporate.getRegistrationNumber())
                    .companyType(corporate.getCompanyType())
                    .sector(corporate.getSector())
                    .monthlyRevenue(corporate.getMonthlyRevenue());
        }

        return builder.build();
    }

    public static UpdateClientResponse mapToUpdateResponse(Client client) {
        return UpdateClientResponse.builder()
                .id(client.getId())
                .clientNumber(client.getClientNumber())
                .clientType(client.getClientType())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .notes(client.getNotes())
                .taxType(client.getTaxType())
                .status(client.getStatus())
                .isActive(client.getIsActive())
                .updatedAt(LocalDateTime.now())
                .fullName(client.getFullName())
                .nationalityNumber(client.getNationalityNumber())
                .passportNo(client.getPassportNo())
                .blueCardNo(client.getBlueCardNo())
                .taxId(client.getTaxId())
                .companyName(client.getCompanyName())
                .taxNumber(client.getTaxNumber())
                .registrationNumber(client.getRegistrationNumber())
                .companyType(client.getCompanyType())
                .sector(client.getSector())
                .build();
    }

    public static ClientSearchResponse mapToClientSearchResponse(Client request) {
        if (request == null) {
            return null;
        }
        return ClientSearchResponse.builder()
                .id(request.getId())
                .taxType(request.getTaxType())
                .taxId(request.getTaxId())
                .nationalityNumber(request.getNationalityNumber())
                .phoneNumber(request.getPhone())
                .email(request.getEmail())
                .clientStatus(request.getStatus())
                .clientNumber(request.getClientNumber())
                .clientType(request.getClientType())
                .fullName(request.getFullName())
                .isActive(request.getIsActive())
                .build();

    }

    public static ClientDTO toClientDTO(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .clientType(client.getClientType())
                .email(client.getEmail())
                .phone(client.getPhone())
                .address(client.getAddress())
                .notes(client.getNotes())
                .status(client.getStatus())
                .isActive(client.getIsActive())
                .taxType(client.getTaxType())
                .createdAt(client.getCreatedAt())
                // Bireysel müşteri alanları
                .fullName(client.getFullName())
                .nationalityType(client.getNationalityType())
                .taxId(client.getTaxId())
                .passportNo(client.getPassportNo())
                .blueCardNo(client.getBlueCardNo())
                .nationalityNumber(client.getNationalityNumber())
                .birthDate(client.getBirthDate())
                .profession(client.getProfession())
                .gender(client.getGender())
                .educationStatus(client.getEducationStatus())
                .monthlyIncome(client.getMonthlyIncome())
                .estimatedTransactionVolume(client.getEstimatedTransactionVolume())
                // Kurumsal müşteri alanları
                .companyName(client.getCompanyName())
                .taxNumber(client.getTaxNumber())
                .registrationNumber(client.getRegistrationNumber())
                .companyType(client.getCompanyType())
                .sector(client.getSector())
                .monthlyRevenue(client.getMonthlyRevenue())
                .build();
    }

}
