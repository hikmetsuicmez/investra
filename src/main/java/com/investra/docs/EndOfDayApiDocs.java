package com.investra.docs;

import com.investra.dtos.response.ClientValuationResponse;
import com.investra.dtos.response.EndOfDayStatusResponse;
import com.investra.dtos.response.Response;
import com.investra.dtos.response.StockPriceResponse;
import com.investra.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Tag(name = "Gün Sonu İşlemleri", description = "Gün sonu değerleme ve kapanış fiyatları ile ilgili işlemler")
public interface EndOfDayApiDocs {

    @Operation(summary = "Gün Sonu Durumunu Getir",
            description = "Gün sonu değerleme durumunu getirir. Eğer değerleme tamamlanmamışsa, " +
                    "son kapanış fiyatlarını alır ve değerlemeyi başlatır.")
    @ApiResponse(responseCode = "200", description = "Gün sonu durumu başarıyla alındı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Gün sonu durumu alınırken bir hata oluştu", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    Response<EndOfDayStatusResponse> getEndOfDayStatus();

    @Operation(summary = "Kapanış Fiyatlarını Al",
            description = "En son kapanış fiyatlarını Infina API'den alır ve veritabanına kaydeder.")
    @ApiResponse(responseCode = "200", description = "Kapanış fiyatları başarıyla alındı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Kapanış fiyatları alınırken bir hata oluştu", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    Response<Boolean> fetchLatestClosingPrices();

    @Operation(summary = "Gün Sonu Değerleme Başlat",
            description = "Gün sonu değerleme işlemini başlatır. " +
                    "Bu işlem, tüm müşteri hesaplarının değerlemesini yapar ve sonuçları kaydeder.")
    @ApiResponse(responseCode = "200", description = "Gün sonu değerleme başarıyla başlatıldı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "400", description = "Gün sonu değerleme zaten başlatılmış", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Gün sonu değerleme başlatılırken bir hata oluştu", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    Response<Boolean> startEndOfDayValuation(@Parameter UserDetails userDetails);

    @Operation(summary = "Tüm Müşteri Değerlemelerini Getir",
            description = "Tüm müşteri hesaplarının değerlemelerini getirir.")
    @ApiResponse(responseCode = "200", description = "Tüm müşteri değerlemeleri başarıyla alındı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Müşteri değerlemeleri alınırken bir hata oluştu", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri değerlemeleri bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Erişim reddedildi", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Yetkisiz erişim", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Servis geçici olarak kullanılamıyor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "504", description = "Zaman aşımı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "408", description = "İstek zaman aşımına uğradı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    Response<List<ClientValuationResponse>> getClientValuations();

    @Operation(summary = "Belirli Bir Müşteri Değerlemesini Getir",
            description = "Belirli bir müşteri hesabının değerlemesini getir.")
    @ApiResponse(responseCode = "200", description = "Müşteri değerlemesi başarıyla alındı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Erişim reddedildi", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Yetkisiz erişim", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Servis geçici olarak kullanılamıyor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "504", description = "Zaman aşımı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "408", description = "İstek zaman aşımına uğradı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Müşteri değerlemesi alınırken bir hata oluştu", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @Parameter(name = "clientId", description = "Müşteri ID'si", required = true)
    Response<ClientValuationResponse> getClientValuation(@Parameter Long clientId);

    @Operation(summary = "Kapanış Fiyatlarını Getir",
            description = "Belirli bir tarihteki kapanış fiyatlarını getirir. " +
                    "Eğer tarih belirtilmezse, bugünün tarihini kullanır.")
    @ApiResponse(responseCode = "200", description = "Kapanış fiyatları başarıyla alındı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Kapanış fiyatları bulunamadı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Erişim reddedildi", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Yetkisiz erişim", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Servis geçici olarak kullanılamıyor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "504", description = "Zaman aşımı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "408", description = "İstek zaman aşımına uğradı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Kapanış fiyatları alınırken bir hata oluştu", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    Response<List<StockPriceResponse>> getStockPrices(
            @Parameter String date);

    @Operation(summary = "Manuel Kapanış Fiyatlarını Güncelle",
            description = "Kapanış fiyatlarını manuel olarak günceller. " +
                    "Bu işlem, Infina API'den alınan fiyatları kullanarak veritabanındaki kapanış fiyatlarını günceller.")
    @ApiResponse(responseCode = "200", description = "Kapanış fiyatları başarıyla güncellendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Kapanış fiyatları güncellenirken bir hata oluştu", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Erişim reddedildi", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Yetkisiz erişim", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "503", description = "Servis geçici olarak kullanılamıyor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "504", description = "Zaman aşımı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "408", description = "İstek zaman aşımına uğradı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Geçersiz istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Çakışma hatası", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "İşleme alınamayan istek", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "408", description = "İstek zaman aşımına uğradı", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    Response<Boolean> manuallyUpdateClosingPrices();

}