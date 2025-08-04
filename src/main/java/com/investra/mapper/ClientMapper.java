package com.investra.mapper;

import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.request.CreateCorporateClientRequest;
import com.investra.dtos.request.CreateIndividualClientRequest;
import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.response.ClientSearchResponse;
import java.time.LocalDateTime;
import java.util.Optional;

import com.investra.dtos.response.CreateClientResponse;
import com.investra.entity.Client;
import com.investra.entity.User;
import com.investra.enums.ClientType;

public class ClientMapper {

    public static Client mapToEntity(CreateClientRequest request, User user){
        Client.ClientBuilder builder = Client.builder()
                .clientType(request.getClientType())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
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
                    .prosfession(individual.getProfession())
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

    public static ClientSearchResponse mapToClientSearchResponse(Client request) {
        if (request == null) {
            return null;
        }
        return ClientSearchResponse.builder()
                .taxId(request.getTaxId())
                .nationalityNumber(request.getNationalityNumber())
                .phoneNumber(request.getPhone())
                .email(request.getEmail())
                .clientStatus(request.getStatus())
                .clientType(request.getClientType())
                .fullName(request.getFullName())
                .build();

    }
}
