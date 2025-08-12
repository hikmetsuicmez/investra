package com.investra.docs;

import com.investra.dtos.request.CreateUserRequest;
import com.investra.dtos.request.UpdateUserRequest;
import com.investra.dtos.response.CreateUserResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.UpdateUserResponse;
import com.investra.dtos.response.UserDTO;
import com.investra.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Yönetici API'ları", description = "Yönetici işlemleri ile ilgili uç noktalar")
public interface AdminApiDocs {

        @Operation(summary = "Kullanıcı Oluştur", description = "Yeni bir kullanıcı oluşturur. Yalnızca yönetici erişimine sahip kullanıcılar tarafından kullanılabilir.")
        @ApiResponse(responseCode = "201", description = "Kullanıcı başarıyla oluşturuldu.", content = @Content(schema = @Schema(implementation = CreateUserResponse.class)))
        @ApiResponse(responseCode = "400", description = "Geçersiz istek.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "500", description = "Sunucu hatası.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        ResponseEntity<Response<CreateUserResponse>> createUser(@Parameter CreateUserRequest request);

        @Operation(summary = "Kullanıcı Güncelle", description = "Mevcut bir kullanıcıyı günceller. Yalnızca yönetici erişimine sahip kullanıcılar tarafından kullanılabilir.")
        @ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla güncellendi.", content = @Content(schema = @Schema(implementation = UpdateUserResponse.class)))
        @ApiResponse(responseCode = "400", description = "Geçersiz istek.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "500", description = "Sunucu hatası.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @Parameter(name = "employeeNumber", description = "Güncellenecek kullanıcının çalışan numarası")
        ResponseEntity<Response<UpdateUserResponse>> updateUser(@Parameter String employeeNumber,
                        @Parameter UpdateUserRequest request);

        @Operation(summary = "Kullanıcı Sil", description = "Mevcut bir kullanıcıyı siler. Yalnızca yönetici erişimine sahip kullanıcılar tarafından kullanılabilir.")
        @ApiResponse(responseCode = "204", description = "Kullanıcı başarıyla silindi.", content = @Content)
        @ApiResponse(responseCode = "400", description = "Geçersiz istek.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "500", description = "Sunucu hatası.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @Parameter(name = "employeeNumber", description = "Silinecek kullanıcının çalışan numarası")
        ResponseEntity<Response<Void>> deleteUser(@Parameter String employeeNumber);

        @Operation(summary = "Tüm Kullanıcıları Getir", description = "Sistemdeki tüm kullanıcıları getirir.")
        @ApiResponse(responseCode = "200", description = "Kullanıcılar başarıyla getirildi.", content = @Content(schema = @Schema(implementation = List.class)))
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "500", description = "Sunucu hatası.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<Response<List<UserDTO>>> retrieveAllUsers();

        @Operation(summary = "Kullanıcıyı ID ile Getir", description = "Belirli bir kullanıcıyı ID ile getirir.")
        @ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla getirildi.", content = @Content(schema = @Schema(implementation = UserDTO.class)))
        @ApiResponse(responseCode = "400", description = "Geçersiz istek.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "401", description = "Yetkisiz erişim.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        @ApiResponse(responseCode = "500", description = "Sunucu hatası.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        public ResponseEntity<Response<UserDTO>> retrieveUser(
                        @Parameter(description = "Kullanıcının ID'si") Long userId);

}
