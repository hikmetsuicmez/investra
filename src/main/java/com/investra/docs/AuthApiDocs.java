package com.investra.docs;

import com.investra.dtos.request.ChangePasswordRequest;
import com.investra.dtos.request.LoginRequest;
import com.investra.dtos.request.ResetPasswordRequest;
import com.investra.dtos.response.LoginResponse;
import com.investra.dtos.response.Response;
import com.investra.exception.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Kimlik Doğrulama API'ları", description = "Kullanıcı kimlik doğrulama ve şifre işlemleri ile ilgili uç noktalar")
public interface AuthApiDocs {

    @Operation(summary = "Kullanıcı Girişi",
            description = "Kullanıcı kimlik bilgileri ile giriş yapar ve erişim belirteci alır.")
    @ApiResponse(responseCode = "200", description = "Başarılı giriş", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri", content = @Content(schema = @Schema(implementation = InvalidCredentialsException.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<LoginResponse>> login(@Parameter LoginRequest loginRequest);

    @Operation(summary = "Şifre Değiştirme",
            description = "Kullanıcının mevcut şifresini yeni bir şifre ile değiştirir.")
    @ApiResponse(responseCode = "200", description = "Şifre başarıyla değiştirildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Geçersiz kimlik bilgileri", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<Void>> changePassword(@Parameter ChangePasswordRequest request);

    @Operation(summary = "Şifre Sıfırlama İsteği",
            description = "Kullanıcının e-posta adresine şifre sıfırlama bağlantısı gönderir.")
    @ApiResponse(responseCode = "200", description = "Şifre sıfırlama isteği başarıyla gönderildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    ResponseEntity<Response<Void>> forgotPassword(@Parameter String email);

    @Operation(summary = "Şifre Sıfırlama",
            description = "Kullanıcının yeni bir şifre belirlemesi için gerekli işlemleri yapar.")
    @ApiResponse(responseCode = "200", description = "Şifre başarıyla sıfırlandı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Geçersiz veya süresi dolmuş token", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı", content = @Content(schema = @Schema(implementation = UserNotFoundException.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Exception.class)))
    ResponseEntity<Response<Void>> resetPassword(
            @Parameter ResetPasswordRequest request,
            @Parameter String token);

}
