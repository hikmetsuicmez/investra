package com.investra.docs;

import com.investra.dtos.request.AccountCreationRequest;
import com.investra.dtos.request.ClientSearchForAccountRequest;
import com.investra.dtos.response.AccountResponse;
import com.investra.dtos.response.ClientForAccountResponse;
import com.investra.dtos.response.Response;
import com.investra.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Account API'ları", description = "Hesap yönetimi ile ilgili API'lar")
public interface AccountApiDocs {


    @Operation(summary = "Yeni hesap oluşturma",
            description = "Yeni bir hesap oluşturur ve gerekli bilgileri kaydeder.")
    @ApiResponse(responseCode = "200", description = "Hesap başarıyla oluşturuldu", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<AccountResponse>> createAccount(@Parameter AccountCreationRequest request);

    @Operation(summary = "Hesap detaylarını getirme",
            description = "Verilen hesap ID'sine göre hesap detaylarını getirir.")
    @ApiResponse(responseCode = "200", description = "Hesap detayları başarıyla getirildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Hesap bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @Parameter(name = "accountId", description = "Hesap ID'si", required = true)
    ResponseEntity<Response<AccountResponse>> getAccountById(@Parameter Long accountId);

    @Operation(summary = "Müşteri hesaplarını getirme",
            description = "Verilen müşteri ID'sine göre müşterinin tüm hesaplarını getirir.")
    @ApiResponse(responseCode = "200", description = "Müşteri hesapları başarıyla getirildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @Parameter(name = "clientId", description = "Müşteri ID'si", required = true)
    ResponseEntity<Response<List<AccountResponse>>> getAccountsByClientId(@Parameter Long clientId);

    @Operation(summary = "Hesap açılışı için müşteri arama",
            description = "Hesap açılışı için gerekli müşteri bilgilerini arar.")
    @ApiResponse(responseCode = "200", description = "Müşteri arama sonuçları başarıyla getirildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<List<ClientForAccountResponse>>> searchClientsForAccount(@Parameter ClientSearchForAccountRequest request);

    @Operation(summary = "En son eklenen müşterileri getirme",
            description = "Hesap açılışı sayfası için en son eklenen müşterileri getirir.")
    @ApiResponse(responseCode = "200", description = "En son eklenen müşteriler başarıyla getirildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<List<ClientForAccountResponse>>> getRecentClients(@Parameter int limit);
}
