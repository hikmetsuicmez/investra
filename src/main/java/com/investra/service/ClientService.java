package com.investra.service;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.response.ClientDTO;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;
import com.investra.entity.Client;

import java.util.List;

public interface ClientService {
    Response<CreateClientResponse> createClient(CreateClientRequest createClientRequest, String userEmail);
    Response<List<ClientDTO>> getActiveClients();
    Response<List<ClientDTO>> getInactiveClients();
    Response<Void> deleteClient(Client client);
    Client findEntityById(Long id);
    Response<List<ClientSearchResponse>> searchClients(ClientSearchRequest request);

}
