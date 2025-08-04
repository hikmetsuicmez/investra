package com.investra.docs;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.StockBuyOrderRequest;
import com.investra.dtos.response.*;
import com.investra.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Tag(name = "Hisse Senedi Alım API'ları", description = "Hisse senedi alım işlemleri ile ilgili uç noktalar")
public interface StockBuyApiDocs {

    @Operation(summary = "Müşteri Arama", description = "Belirli kriterlere göre müşteri arama işlemi yapar.")
    @ApiResponse(responseCode = "200", description = "Müşteri arama sonuçları başarıyla döndürüldü", content = @Content
            (schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    Response<List<ClientSearchResponse>> searchClients(@Parameter ClientSearchRequest request);

    @Operation(summary = "Mevcut Hisse Senetleri", description = "Sistemdeki mevcut hisse senetlerini getirir.")
    @ApiResponse(responseCode = "200", description = "Mevcut hisse senetleri başarıyla döndürüldü", content = @Content
            (schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Mevcut hisse senetleri bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response<List<StockResponse>> getAvailableStocks();

    @Operation(summary = "Hisse Senedi Alım Önizleme", description = "Hisse senedi alım işlemi için önizleme yapar.")
    @ApiResponse(responseCode = "200", description = "Hisse senedi alım önizleme başarıyla döndürüldü", content = @Content
            (schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri veya hisse senedi bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response<StockBuyOrderPreviewResponse> previewBuyOrder(@Parameter StockBuyOrderRequest request);

    @Operation(summary = "Hisse Senedi Alım İşlemi", description = "Hisse senedi alım işlemini gerçekleştirir.")
    @ApiResponse(responseCode = "200", description = "Hisse senedi alım işlemi başarıyla gerçekleştirildi", content = @Content
            (schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri veya hisse senedi bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public Response<StockBuyOrderResultResponse> executeBuyOrder(
            @Parameter StockBuyOrderRequest request,
            @Parameter UserDetails userDetails);

}
