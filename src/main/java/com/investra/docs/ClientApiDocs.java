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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Tag(name = "Müşteri API'ları", description = "Müşteri ile ilgili işlemler için uç noktalar")
public interface ClientApiDocs {

    @Operation(summary = "Müşteri Oluşturma", description = "Yeni bir müşteri oluşturur. Müşteri tipi 'INDIVIDUAL' veya 'CORPORATE' olabilir.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Müşteri oluşturma isteği, JSON formatında gönderilir.", required = true, content = @Content(schema = @Schema(implementation = CreateClientRequest.class))))
    @ApiResponse(responseCode = "201", description = "Müşteri başarıyla oluşturuldu", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek veya müşteri tipi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "401", description = "Yetkilendirme hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    public ResponseEntity<Response<CreateClientResponse>> createUser(@Parameter Map<String, Object> payload);

    @Operation(summary = "Müşteriyi Kimlik Bilgisine Göre Getir", description = "TCKN, Pasaport No, Mavi Kart No, Vergi No veya Sicil No bilgisiyle müşteriyi sorgular.")
    @ApiResponse(responseCode = "200", description = "Müşteri başarıyla bulundu", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    @Parameter(name = "nationalityNumber", description = "TCKN (Türkiye Cumhuriyeti Kimlik Numarası)", required = false)
    public ResponseEntity<Response<ClientSearchResponse>> findClient(
            @Parameter String nationalityNumber,
            @Parameter String passportNo,
            @Parameter String blueCardNo,
            @Parameter String taxNumber,
            @Parameter String registrationNumber
    );
}
