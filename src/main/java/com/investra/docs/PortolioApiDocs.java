package com.investra.docs;

import com.investra.dtos.request.PortfolioCreateRequest;
import com.investra.dtos.response.PortfolioDTO;
import com.investra.dtos.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Portfolio API'ları", description = "Portföy yönetimi API'ları")
public interface PortolioApiDocs {


    @Operation(summary = "Portföy Oluştur",
               description = "Yeni bir portföy oluşturur. Portföy, bir müşterinin yatırım hesaplarını ve varlıklarını yönetmek için kullanılır.")
    @ApiResponse(responseCode = "201", description = "Portföy başarıyla oluşturuldu.", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek. Portföy oluşturma isteği geçersiz.", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası. Portföy oluşturma işlemi sırasında beklenmeyen bir hata oluştu.", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<PortfolioDTO>> createPortfolio(@Parameter PortfolioCreateRequest request);

    @Operation(summary = "Tüm Portföyleri Getir",
               description = "Tüm portföyleri listeler. Bu, tüm müşterilerin portföylerini görüntülemek için kullanılır.")
    @ApiResponse(responseCode = "200", description = "Portföyler başarıyla getirildi.", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası. Portföyleri getirme işlemi sırasında beklenmeyen bir hata oluştu.", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<PortfolioDTO>>> getAllPortfolio();

    @Operation(summary = "Müşteri ID'sine Göre Portföy Getir",
               description = "Belirli bir müşteri ID'sine göre portföyü getirir. Bu, bir müşterinin portföyünü görüntülemek için kullanılır.")
    @ApiResponse(responseCode = "200", description = "Portföy başarıyla getirildi.", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Portföy bulunamadı. Belirtilen müşteri ID'sine ait portföy bulunamadı.", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası. Portföyü getirme işlemi sırasında beklenmeyen bir hata oluştu.", content = @Content(schema = @Schema(implementation = Response.class)))
    @Parameter(name = "clientId", description = "Müşteri ID'si", required = true)
    ResponseEntity<Response<PortfolioDTO>> getPortfolioByClientId(@Parameter Long clientId);

    @Operation(summary = "Müşteri ID'sine Göre Portföy Sil",
               description = "Belirli bir müşteri ID'sine göre portföyü siler. Bu, bir müşterinin portföyünü kaldırmak için kullanılır.")
    @ApiResponse(responseCode = "200", description = "Portföy başarıyla silindi.", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Portföy bulunamadı. Belirtilen müşteri ID'sine ait portföy bulunamadı.", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası. Portföy silme işlemi sırasında beklenmeyen bir hata oluştu.", content = @Content(schema = @Schema(implementation = Response.class)))
    @Parameter(name = "clientId", description = "Müşteri ID'si", required = true)
    ResponseEntity<Response<Void>> deletePortfolioByClientId(@Parameter Long clientId);
}
