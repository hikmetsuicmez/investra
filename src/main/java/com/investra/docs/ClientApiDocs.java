package com.investra.docs;

import com.investra.dtos.request.CreateClientRequest;
import com.investra.dtos.response.ClientSearchResponse;
import com.investra.dtos.response.CreateClientResponse;
import com.investra.dtos.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "Müşteri API'ları", description = "Müşteri ile ilgili işlemler için uç noktalar")
public interface ClientApiDocs {

    @Operation(summary = "Müşteri Oluşturma", description = "Yeni bir müşteri oluşturur. Müşteri tipi 'INDIVIDUAL' veya 'CORPORATE' olabilir.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Müşteri oluşturma isteği, JSON formatında gönderilir.", required = true, content = @Content(schema = @Schema(implementation = CreateClientRequest.class))))
    @ApiResponse(responseCode = "201", description = "Müşteri başarıyla oluşturuldu", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya müşteri tipi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<CreateClientResponse>> createClient(@Parameter(description = "Müşteri oluşturma verileri (JSON)", required = true) Map<String, Object> payload);

    @Operation(summary = "Müşteriyi Kimlik Bilgisine Göre Getir", description = "TCKN, Pasaport No, Mavi Kart No, Vergi No veya Sicil No bilgisiyle müşteriyi sorgular.")
    @ApiResponse(responseCode = "200", description = "Müşteri başarıyla bulundu", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<ClientSearchResponse>> getClientByIdentifier(@Parameter(description = "T.C. Kimlik Numarası", required = false) @org.springframework.web.bind.annotation.RequestParam(required = false) String taxId, @Parameter(description = "Pasaport Numarası", required = false) @org.springframework.web.bind.annotation.RequestParam(required = false) String passportNo, @Parameter(description = "Mavi Kart Numarası", required = false) @org.springframework.web.bind.annotation.RequestParam(required = false) String blueCardNo, @Parameter(description = "Vergi Numarası", required = false) @org.springframework.web.bind.annotation.RequestParam(required = false) String taxNumber, @Parameter(description = "Sicil Numarası", required = false) @org.springframework.web.bind.annotation.RequestParam(required = false) String registrationNumber);


}
