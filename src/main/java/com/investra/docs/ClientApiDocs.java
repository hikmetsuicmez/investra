package com.investra.docs;

import com.investra.dtos.request.ClientSearchRequest;
import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.response.ClientDTO;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

@Tag(name = "Müşteri API'ları", description = "Müşteri ile ilgili işlemler için uç noktalar")
public interface ClientApiDocs {

    @Operation(
            summary = "Müşteri Oluşturma",
            description = "Yeni bir müşteri oluşturur. Müşteri tipi 'INDIVIDUAL' veya 'CORPORATE' olabilir.",
            requestBody = @RequestBody(
                    description = "Müşteri oluşturma isteği, JSON formatında gönderilir. clientType alanı zorunludur.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateClientRequest.class))
            )
    )
    @ApiResponse(responseCode = "201", description = "Müşteri başarıyla oluşturuldu", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya müşteri tipi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<CreateClientResponse>> createClient(
            @Parameter(description = "Müşteri oluşturma verileri (JSON)", required = true) Map<String, Object> payload
    );

    @Operation(
            summary = "Müşteri Bilgisi Getir",
            description = "Verilen kriterlere göre müşteriyi arar ve ilk bulunan müşterinin bilgilerini döner.",
            requestBody = @RequestBody(
                    description = "Müşteri arama kriterleri",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientSearchRequest.class))
            )
    )
    @ApiResponse(responseCode = "200", description = "Müşteri bulundu", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<ClientSearchResponse>> findClient(
            @Parameter(description = "Müşteri arama kriterleri", required = true) ClientSearchRequest request
    );

    @Operation(
            summary = "Müşteri Silme (Pasif Yapma)",
            description = "Verilen kriterlere göre müşteriyi arar ve bulunan ilk müşteriyi siler (pasif yapar).",
            requestBody = @RequestBody(
                    description = "Silinecek müşteriyi belirlemek için arama kriterleri",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ClientSearchRequest.class))
            )
    )
    @ApiResponse(responseCode = "200", description = "Müşteri başarıyla silindi (pasif yapıldı)", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<Void>> deleteClient(
            @Parameter(description = "Silinecek müşteri arama kriterleri", required = true) ClientSearchRequest request
    );

    @Operation(
            summary = "Aktif Müşteri Listesi",
            description = "Sistemde aktif durumda olan tüm müşterileri listeler."
    )
    @ApiResponse(responseCode = "200", description = "Aktif müşteriler listelendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<ClientDTO>>> getActiveClients();

    @Operation(
            summary = "Pasif Müşteri Listesi",
            description = "Sistemde pasif durumda olan tüm müşterileri listeler."
    )
    @ApiResponse(responseCode = "200", description = "Pasif müşteriler listelendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<ClientDTO>>> getInactiveClients();

}
