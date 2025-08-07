package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.PortolioApiDocs;
import com.investra.dtos.request.PortfolioCreateRequest;
import com.investra.dtos.response.PortfolioDTO;
import com.investra.dtos.response.Response;
import com.investra.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.Portfolio.BASE)
@RequiredArgsConstructor
public class PortfolioController implements PortolioApiDocs {

    private final PortfolioService portfolioService;

    @PostMapping(ApiEndpoints.Portfolio.CREATE)
    public ResponseEntity<Response<PortfolioDTO>> createPortfolio(@RequestBody @Valid PortfolioCreateRequest request) {
        return ResponseEntity.ok(portfolioService.createPortfolio(request));
    }

    @GetMapping(ApiEndpoints.Portfolio.GET_ALL)
    public ResponseEntity<Response<List<PortfolioDTO>>> getAllPortfolio() {
        return ResponseEntity.ok(portfolioService.getAllPortfolio());
    }

    @GetMapping(ApiEndpoints.Portfolio.GET_BY_CLIENT_ID)
    public ResponseEntity<Response<PortfolioDTO>> getPortfolioByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(portfolioService.getPortfolioByClientId(clientId));
    }

    @DeleteMapping(ApiEndpoints.Portfolio.DELETE_BY_CLIENT_ID)
    public ResponseEntity<Response<Void>> deletePortfolioByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(portfolioService.deletePortfolioByClientId(clientId));
    }

}
