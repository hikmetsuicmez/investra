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
        var strategy = getStringOptionalFunction(request);
        List<Client> clients = strategy != null
                ? strategy.apply(request.getSearchTerm()).map(List::of).orElse(List.of())
                : List.of();

        List<ClientSearchResponse> responseClients = clients.stream()
                .map(ClientMapper::mapToClientSearchResponse)
                .toList();

        return Response.<List<ClientSearchResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .isSuccess(true)
                .message("Müşteri arama işlemi tamamlandı")
                .data(responseClients)
                .build();
    }

    protected Function<String, Optional<Client>> getStringOptionalFunction(ClientSearchRequest request) {
        Map<String, Function<String, Optional<Client>>> searchStrategy = Map.of(
                "TCKN", clientRepository::findByNationalityNumber,
                "VERGI_NO", clientRepository::findByTaxId,
                "MAVI_KART_NO", clientRepository::findByBlueCardNo,
                "ISIM", this::findClientByName
        );

        return searchStrategy.get(request.getSearchType());
    }

    private Optional<Client> findClientByName(String name) {
        return clientRepository.findAll().stream()
                .filter(client -> client.getFullName().toLowerCase().contains(name.toLowerCase()))
                .findFirst();
    }

}
