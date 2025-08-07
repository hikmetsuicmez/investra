package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.docs.EndOfDayApiDocs;
import com.investra.dtos.response.ClientValuationResponse;
import com.investra.dtos.response.EndOfDayStatusResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockPriceResponse;
import com.investra.service.EndOfDayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.EndOfDay.BASE)
@RequiredArgsConstructor
@Slf4j
public class EndOfDayController implements EndOfDayApiDocs {

    private final EndOfDayService endOfDayService;

    @GetMapping(ApiEndpoints.EndOfDay.STATUS)
    public Response<EndOfDayStatusResponse> getEndOfDayStatus() {
        log.info("Gün sonu durumu sorgulanıyor");
        EndOfDayStatusResponse status = endOfDayService.getEndOfDayStatus();

        return Response.<EndOfDayStatusResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Gün sonu durumu başarıyla alındı")
                .data(status)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.FETCH_PRICES)
    public Response<Boolean> fetchLatestClosingPrices() {
        log.info("Kapanış fiyatları alınıyor");
        boolean success = endOfDayService.fetchLatestClosingPrices();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message(success ? "Kapanış fiyatları başarıyla alındı" : "Kapanış fiyatları alınamadı")
                .data(success)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.START_VALUATION)
    public Response<Boolean> startEndOfDayValuation(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("Gün sonu değerleme başlatılıyor");
        endOfDayService.runEndOfDayValuation(userDetails.getUsername());

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Gün sonu değerleme tamamlandı")
                .data(true)
                .build();
    }

    @GetMapping(ApiEndpoints.EndOfDay.CLIENT_VALUATIONS)
    public Response<List<ClientValuationResponse>> getClientValuations() {
        log.info("Tüm müşteri değerlemeleri alınıyor");
        List<ClientValuationResponse> valuations = endOfDayService.getAllClientValuations();

        return Response.<List<ClientValuationResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri değerlemeleri başarıyla alındı")
                .data(valuations)
                .build();
    }

    @GetMapping(ApiEndpoints.EndOfDay.CLIENT_VALUATION)
    public Response<ClientValuationResponse> getClientValuation(@PathVariable Long clientId) {
        log.info("Müşteri değerlemesi alınıyor: {}", clientId);
        ClientValuationResponse valuation = endOfDayService.getClientValuation(clientId);

        return Response.<ClientValuationResponse>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Müşteri değerlemesi başarıyla alındı")
                .data(valuation)
                .build();
    }

    @GetMapping(ApiEndpoints.EndOfDay.STOCK_PRICES)
    public Response<List<StockPriceResponse>> getStockPrices(
            @RequestParam(required = false) String date) {

        LocalDate priceDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        log.info("Hisse fiyatları alınıyor: {}", priceDate);

        List<StockPriceResponse> prices = endOfDayService.getAllStockPrices(priceDate);

        return Response.<List<StockPriceResponse>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Hisse fiyatları başarıyla alındı")
                .data(prices)
                .build();
    }

    @PostMapping(ApiEndpoints.EndOfDay.MANUALLY_UPDATE_PRICES)
    public Response<Boolean> manuallyUpdateClosingPrices() {
        log.info("Kapanış fiyatları manuel olarak güncelleniyor");
        boolean success = endOfDayService.manuallyUpdateClosingPrices();

        return Response.<Boolean>builder()
                .statusCode(HttpStatus.OK.value())
                .message(success ? "Gün sonu fiyatları başarıyla güncellendi" : "Gün sonu fiyatları güncellenemedi")
                .data(success)
                .build();
    }
}
