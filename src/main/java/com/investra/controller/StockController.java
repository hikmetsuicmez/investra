package com.investra.controller;

import com.investra.dtos.response.Response;
import com.investra.entity.Stock;
import com.investra.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StockController {

    private final StockService stockService;

    // Tüm hisse senetlerini getirir
    @GetMapping
    public ResponseEntity<Response<List<Stock>>> getAllStocks() {
        log.info("Tüm hisse senetleri için istek alındı");
        List<Stock> stocks = stockService.getAllStocks();

        return ResponseEntity.ok(
                Response.<List<Stock>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Hisse senetleri başarıyla getirildi")
                        .data(stocks)
                        .build()
        );
    }

    // Belirli bir hisse senedini kodu ile getirir
    @GetMapping("/{stockCode}")
    public ResponseEntity<Response<Stock>> getStockByCode(@PathVariable String stockCode) {
        log.info("{} kodlu hisse senedi için istek alındı", stockCode);
        Optional<Stock> stockOpt = stockService.getStockByCode(stockCode);

        if (stockOpt.isPresent()) {
            return ResponseEntity.ok(
                    Response.<Stock>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message("Hisse senedi başarıyla getirildi")
                            .data(stockOpt.get())
                            .build()
            );
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Response.<Stock>builder()
                            .statusCode(HttpStatus.NOT_FOUND.value())
                            .message("Hisse senedi bulunamadı: " + stockCode)
                            .build()
            );
        }
    }

    // Hisse senedi verilerini API'den manuel olarak yeniler
    @PostMapping("/refresh")
    public ResponseEntity<Response<List<Stock>>> refreshStocks() {
        log.info("Hisse senedi verilerini yenileme isteği alındı");
        List<Stock> stocks = stockService.refreshStocksFromApi();

        return ResponseEntity.ok(
                Response.<List<Stock>>builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Hisse senedi verileri başarıyla yenilendi")
                        .data(stocks)
                        .build()
        );
    }
}
