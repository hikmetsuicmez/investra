package com.investra.docs;

import com.investra.dtos.request.WithdrawalRequest;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.WithdrawalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Hesaptan Para Çekme API", description = "Hesaptan para çekme işlemleri için API dokümantasyonu")
public interface AccountWithdrawalApiDocs {


    @Operation(summary = "Hesaptan Para Çekme",
            description = "Belirtilen hesap ve miktar için para çekme işlemi gerçekleştirir.")
    @ApiResponse(responseCode = "200", description = "Para çekme işlemi başarılı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Hesap bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<WithdrawalResponse>> withdrawFromAccount(
            @Valid @RequestBody WithdrawalRequest request,
            @AuthenticationPrincipal UserDetails userDetails);
}
