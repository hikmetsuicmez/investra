package com.investra.service;

import com.investra.dtos.response.PortfolioReportResponse;
import com.investra.dtos.response.PortfolioSummaryReportResponse;
import com.investra.dtos.response.PortfolioPerformanceReportResponse;
import com.investra.dtos.response.Response;

import java.util.List;

public interface PortfolioReportService {

    /**
     * Müşteri portföy raporunu getirir
     */
    Response<PortfolioReportResponse> getPortfolioReport(Long clientId, String date);

    /**
     * Portföy raporunu Excel formatında export eder
     */
    byte[] exportPortfolioReportToExcel(Long clientId, String date);

    /**
     * Portföy raporunu PDF formatında export eder
     */
    byte[] exportPortfolioReportToPdf(Long clientId, String date);

    /**
     * Tüm müşterilerin portföy raporlarını getirir
     */
    Response<List<PortfolioReportResponse>> getAllPortfolioReports(String date);

    /**
     * Belirli bir tarihteki tüm portföy raporlarını getirir
     */
    Response<List<PortfolioReportResponse>> getPortfolioReportsByDate(String date);

    /**
     * Müşteri tipine göre portföy raporlarını getirir
     */
    Response<List<PortfolioReportResponse>> getPortfolioReportsByClientType(String clientType);

    /**
     * Portföy özet raporunu getirir
     */
    Response<PortfolioSummaryReportResponse> getPortfolioSummaryReport(String date);

    /**
     * Portföy performans raporunu getirir
     */
    Response<PortfolioPerformanceReportResponse> getPortfolioPerformanceReport(String startDate, String endDate);
}
