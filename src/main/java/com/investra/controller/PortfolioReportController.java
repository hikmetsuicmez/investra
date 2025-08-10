package com.investra.controller;

import com.investra.constants.ApiEndpoints;
import com.investra.dtos.response.PortfolioReportResponse;
import com.investra.dtos.response.PortfolioSummaryReportResponse;
import com.investra.dtos.response.PortfolioPerformanceReportResponse;
import com.investra.dtos.response.Response;
import com.investra.docs.PortfolioReportApiDocs;
import com.investra.service.PortfolioReportService;
import com.investra.service.SimulationDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping(ApiEndpoints.PortfolioReport.BASE)
@RequiredArgsConstructor
@Slf4j
public class PortfolioReportController implements PortfolioReportApiDocs {

    private final PortfolioReportService portfolioReportService;
    private final SimulationDateService simulationDateService;

    @GetMapping(ApiEndpoints.PortfolioReport.GET_REPORT)
    public ResponseEntity<Response<PortfolioReportResponse>> getPortfolioReport(
            @PathVariable Long clientId,
            @RequestParam(required = false) String date) {

        log.info("Portföy raporu isteği alındı. ClientId: {}, Date: {}", clientId, date);

        Response<PortfolioReportResponse> response = portfolioReportService.getPortfolioReport(clientId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiEndpoints.PortfolioReport.EXPORT_EXCEL)
    public ResponseEntity<byte[]> exportPortfolioReportToExcel(
            @PathVariable Long clientId,
            @RequestParam(required = false) String date) {

        log.info("Excel export isteği alındı. ClientId: {}, Date: {}", clientId, date);

        try {
            byte[] excelData = portfolioReportService.exportPortfolioReportToExcel(clientId, date);

            String fileName = String.format("portfoy_raporu_%s_%s.xlsx",
                    clientId,
                    date != null ? date
                            : simulationDateService.getCurrentSimulationDate()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);

        } catch (Exception e) {
            log.error("Excel export hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(ApiEndpoints.PortfolioReport.EXPORT_PDF)
    public ResponseEntity<byte[]> exportPortfolioReportToPdf(
            @PathVariable Long clientId,
            @RequestParam(required = false) String date) {

        log.info("PDF export isteği alındı. ClientId: {}, Date: {}", clientId, date);

        try {
            byte[] pdfData = portfolioReportService.exportPortfolioReportToPdf(clientId, date);

            String fileName = String.format("portfoy_raporu_%s_%s.pdf",
                    clientId,
                    date != null ? date
                            : simulationDateService.getCurrentSimulationDate()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", fileName);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfData);

        } catch (Exception e) {
            log.error("PDF export hatası: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(ApiEndpoints.PortfolioReport.GET_ALL_REPORTS)
    public ResponseEntity<Response<List<PortfolioReportResponse>>> getAllPortfolioReports(
            @RequestParam(required = false) String date) {

        log.info("Tüm portföy raporları isteği alındı. Date: {}", date);

        Response<List<PortfolioReportResponse>> response = portfolioReportService.getAllPortfolioReports(date);
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiEndpoints.PortfolioReport.GET_REPORTS_BY_DATE)
    public ResponseEntity<Response<List<PortfolioReportResponse>>> getPortfolioReportsByDate(
            @RequestParam String date) {

        log.info("Tarihe göre portföy raporları isteği alındı. Date: {}", date);

        Response<List<PortfolioReportResponse>> response = portfolioReportService.getPortfolioReportsByDate(date);
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiEndpoints.PortfolioReport.GET_REPORTS_BY_CLIENT_TYPE)
    public ResponseEntity<Response<List<PortfolioReportResponse>>> getPortfolioReportsByClientType(
            @RequestParam String clientType) {

        log.info("Müşteri tipine göre portföy raporları isteği alındı. ClientType: {}", clientType);

        Response<List<PortfolioReportResponse>> response = portfolioReportService
                .getPortfolioReportsByClientType(clientType);
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiEndpoints.PortfolioReport.GET_SUMMARY_REPORT)
    public ResponseEntity<Response<PortfolioSummaryReportResponse>> getPortfolioSummaryReport(
            @RequestParam(required = false) String date) {

        log.info("Portföy özet raporu isteği alındı. Date: {}", date);

        Response<PortfolioSummaryReportResponse> response = portfolioReportService.getPortfolioSummaryReport(date);
        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiEndpoints.PortfolioReport.GET_PERFORMANCE_REPORT)
    public ResponseEntity<Response<PortfolioPerformanceReportResponse>> getPortfolioPerformanceReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("Portföy performans raporu isteği alındı. StartDate: {}, EndDate: {}", startDate, endDate);

        Response<PortfolioPerformanceReportResponse> response = portfolioReportService
                .getPortfolioPerformanceReport(startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
