package com.investra.docs;

import com.investra.dtos.response.PortfolioReportResponse;
import com.investra.dtos.response.PortfolioSummaryReportResponse;
import com.investra.dtos.response.PortfolioPerformanceReportResponse;
import com.investra.dtos.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Portföy Raporları", description = "Portföy raporları ile ilgili API endpoint'leri")
public interface PortfolioReportApiDocs {

    @Operation(summary = "Müşteri Portföy Raporu", description = "Belirli bir müşterinin portföy raporunu getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portföy raporu başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PortfolioReportResponse.class))),
            @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı"),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    ResponseEntity<Response<PortfolioReportResponse>> getPortfolioReport(
            @Parameter(description = "Müşteri ID") @PathVariable Long clientId,
            @Parameter(description = "Rapor tarihi (yyyy-MM-dd formatında, opsiyonel)") @RequestParam(required = false) String date);

    @Operation(summary = "Excel Export", description = "Müşteri portföy raporunu Excel formatında export eder")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel dosyası başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "500", description = "Export hatası")
    })
    ResponseEntity<byte[]> exportPortfolioReportToExcel(
            @Parameter(description = "Müşteri ID") @PathVariable Long clientId,
            @Parameter(description = "Rapor tarihi (yyyy-MM-dd formatında, opsiyonel)") @RequestParam(required = false) String date);

    @Operation(summary = "PDF Export", description = "Müşteri portföy raporunu PDF formatında export eder")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF dosyası başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE)),
            @ApiResponse(responseCode = "500", description = "Export hatası")
    })
    ResponseEntity<byte[]> exportPortfolioReportToPdf(
            @Parameter(description = "Müşteri ID") @PathVariable Long clientId,
            @Parameter(description = "Rapor tarihi (yyyy-MM-dd formatında, opsiyonel)") @RequestParam(required = false) String date);

    @Operation(summary = "Tüm Portföy Raporları", description = "Tüm müşterilerin portföy raporlarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tüm portföy raporları başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PortfolioReportResponse.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    ResponseEntity<Response<List<PortfolioReportResponse>>> getAllPortfolioReports(
            @Parameter(description = "Rapor tarihi (yyyy-MM-dd formatında, opsiyonel)") @RequestParam(required = false) String date);

    @Operation(summary = "Tarihe Göre Portföy Raporları", description = "Belirli bir tarihteki tüm portföy raporlarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tarihe göre portföy raporları başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PortfolioReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz tarih formatı"),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    ResponseEntity<Response<List<PortfolioReportResponse>>> getPortfolioReportsByDate(
            @Parameter(description = "Rapor tarihi (yyyy-MM-dd formatında)") @RequestParam String date);

    @Operation(summary = "Müşteri Tipine Göre Portföy Raporları", description = "Belirli müşteri tipindeki tüm portföy raporlarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Müşteri tipine göre portföy raporları başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PortfolioReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz müşteri tipi"),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    ResponseEntity<Response<List<PortfolioReportResponse>>> getPortfolioReportsByClientType(
            @Parameter(description = "Müşteri tipi (INDIVIDUAL, CORPORATE)") @RequestParam String clientType);

    @Operation(summary = "Portföy Özet Raporu", description = "Tüm portföylerin özet raporunu getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portföy özet raporu başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PortfolioSummaryReportResponse.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    ResponseEntity<Response<PortfolioSummaryReportResponse>> getPortfolioSummaryReport(
            @Parameter(description = "Rapor tarihi (yyyy-MM-dd formatında, opsiyonel)") @RequestParam(required = false) String date);

    @Operation(summary = "Portföy Performans Raporu", description = "Belirli tarih aralığındaki portföy performans raporunu getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portföy performans raporu başarıyla oluşturuldu", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PortfolioPerformanceReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz tarih formatı"),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası")
    })
    ResponseEntity<Response<PortfolioPerformanceReportResponse>> getPortfolioPerformanceReport(
            @Parameter(description = "Başlangıç tarihi (yyyy-MM-dd formatında, opsiyonel)") @RequestParam(required = false) String startDate,
            @Parameter(description = "Bitiş tarihi (yyyy-MM-dd formatında, opsiyonel)") @RequestParam(required = false) String endDate);
}
