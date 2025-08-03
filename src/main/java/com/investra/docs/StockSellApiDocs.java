package com.investra.docs;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockSellOrderRequest;
import com.investra.dtos.response.*;
import com.investra.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Hisse Senedi Satış API'ları", description = "Hisse senedi satış işlemleri ile ilgili uç noktalar")
public interface StockSellApiDocs {

    @Operation(summary = "Müşteri Arama", description = "Belirli kriterlere göre müşteri arama işlemi yapar.")
    @ApiResponse(responseCode = "200", description = "Müşteri arama sonuçları başarıyla döndürüldü", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<List<ClientSearchResponse>>> searchClients(@Parameter ClientSearchRequest request);

    @Operation(summary = "Müşteri Hisse Senedi Envanteri", description = "Belirli bir müşterinin hisse senedi envanterini getirir.")
    @ApiResponse(responseCode = "200", description = "Müşteri hisse senedi envanteri başarıyla döndürüldü", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @Parameter(name = "clientId", description = "Hisse senedi envanterini almak istediğiniz müşteri ID'si")
    ResponseEntity<Response<List<ClientStockHoldingResponse>>> getClientStockHoldings(@Parameter Long clientId);

    @Operation(summary = "Hisse Senedi Satış Önizleme", description = "Hisse senedi satış işlemi için önizleme yapar.")
    @ApiResponse(responseCode = "200", description = "Hisse senedi satış önizleme başarıyla döndürüldü", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri veya hisse senedi bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<StockSellOrderPreviewResponse>> previewSellOrder(@Parameter StockSellOrderRequest request);

    @Operation(summary = "Hisse Senedi Satış İşlemi", description = "Hisse senedi satış işlemini gerçekleştirir.")
    @ApiResponse(responseCode = "200", description = "Hisse senedi satış işlemi başarıyla gerçekleştirildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri veya hisse senedi bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<StockSellOrderResultResponse>> executeSellOrder(@Parameter StockSellOrderRequest request);

}
