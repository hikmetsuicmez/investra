package com.investra.docs;

import com.investra.dtos.request.DepositRequest;
import com.investra.dtos.response.DepositResponse;
import com.investra.dtos.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Hesaba Bakiye Yükleme API", description = "Hesaba bakiye yükleme işlemleri için API dokümantasyonu")
public interface AccountDepositApiDocs {

    @Operation(summary = "Hesaba Bakiye Yükleme",
            description = "Belirtilen müşteri hesabına bakiye yükler. " +
                    "İşlem başarılı olduğunda yeni bakiye bilgisi ile birlikte yanıt döner.")
    @ApiResponse(responseCode = "200", description = "Bakiye yükleme başarılı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "401", description = "Yetkisiz erişim", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Hesap bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<DepositResponse>> depositToAccount(@Parameter DepositRequest request, @Parameter UserDetails userDetails);

}
