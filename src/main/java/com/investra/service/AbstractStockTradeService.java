package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Client;
import com.investra.mapper.ClientMapper;
import com.investra.repository.ClientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public abstract class AbstractStockTradeService implements StockTradeService {

    protected final ClientRepository clientRepository;

    protected AbstractStockTradeService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request) {
        log.info("Client arama işlemi başlatıldı. Arama kriteri: {}, Aktif filtre: {}",
                request.getSearchTerm(), request.getIsActive());

        var strategy = getStringOptionalFunction(request);
        List<Client> clients = strategy != null
                ? strategy.apply(request.getSearchTerm()).map(List::of).orElse(List.of())
                : List.of();

        if (request.getIsActive() != null) {
            clients = clients.stream()
                    .filter(client -> request.getIsActive().equals(client.getIsActive()))
                    .toList();
        }

        List<ClientSearchResponse> responseClients = clients.stream()
                .map(ClientMapper::mapToClientSearchResponse)
                .toList();
        log.info("Müşteri arama işlemi tamamlandı. Dönen müşteri {}", responseClients.get(0));

        return Response.<List<ClientSearchResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri arama işlemi tamamlandı")
                .data(responseClients)
                .build();
    }

    protected Function<String, Optional<Client>> getStringOptionalFunction(ClientSearchRequest request) {
        Map<String, Function<String, Optional<Client>>> searchStrategy = Map.of(
                "MUSTERI_NUMARASI",clientRepository::findByClientNumber,
                "TCKN", clientRepository::findByNationalityNumber,
                "VERGI_ID", clientRepository::findByTaxId,
                "MAVI_KART_NO", clientRepository::findByBlueCardNo,
                "PASSPORT_NO", clientRepository::findByPassportNo,
                "VERGI_NO", clientRepository::findByTaxNumber,
                "ISIM", term -> clientRepository.findByFullNameOrCompanyName(term, term)        );

        return searchStrategy.get(request.getSearchType());
    }

    private Optional<Client> findClientByName(String name) {
        return clientRepository.findAll().stream()
                .filter(client -> client.getFullName().toLowerCase().contains(name.toLowerCase()))
                .findFirst();
    }

}
