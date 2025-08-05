package com.investra.docs;

import com.investra.dtos.response.Response;
import com.investra.entity.Stock;
import com.investra.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Stock API'ları", description = "Hisse senedi yönetimi ile ilgili API'lar")
public interface StockApiDocs {

    @Operation(
            summary = "Tüm hisse senetlerini getirir",
            description = "Bu API, sistemdeki tüm hisse senetlerini listeler."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Hisse senetleri başarıyla getirildi", content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<Response<List<Stock>>> getAllStocks();

    @Operation(
            summary = "Hisse senedini koduna göre getirir",
            description = "Bu API, belirli bir hisse senedini kodu ile getirir."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Hisse senedi başarıyla getirildi", content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Hisse senedi bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @Parameter(
            name = "stockCode",
            description = "Hisse senedinin benzersiz kodu",
            required = true,
            example = "AAPL"
    )
    public ResponseEntity<Response<Stock>> getStockByCode(@PathVariable String stockCode);

    @Operation(
            summary = "Hisse senetlerini günceller",
            description = "Bu API, hisse senedi verilerini günceller ve en son verileri getirir."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Hisse senetleri başarıyla güncellendi", content = @Content(schema = @Schema(implementation = Response.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    public ResponseEntity<Response<List<Stock>>> refreshStocks();
}
