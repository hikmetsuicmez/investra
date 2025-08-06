package com.investra.docs;

import com.investra.dtos.response.Response;
import com.investra.entity.TradeOrder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@Tag(name = "Emir API'ları", description = "Emir işlemleri ile ilgili API'ler")
public interface TradeOrderApiDocs {

    @Operation(
            summary = "Tüm Emirleri Getir",
            description = "Kullanıcının tüm emirlerini getirir."
    )
    @ApiResponse(responseCode = "200", description = "Emirler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Emir bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<TradeOrder>>> getAllOrders(@Parameter UserDetails userDetails);

    @Operation(
            summary = "Bekleyen Emirleri Getir",
            description = "Kullanıcının bekleyen emirlerini getirir."
    )
    @ApiResponse(responseCode = "200", description = "Bekleyen emirler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Bekleyen emir bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<TradeOrder>>> getPendingOrders(@Parameter UserDetails userDetails);

    @Operation(
            summary = "Gerçekleştirilen Emirleri Getir",
            description = "Kullanıcının gerçekleştirilen emirlerini getirir."
    )
    @ApiResponse(responseCode = "200", description = "Gerçekleştirilen emirler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Gerçekleştirilen emir bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<TradeOrder>>> getExecutedOrders(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(
            summary = "Tamamlanmış Emirleri Getir",
            description = "Kullanıcının tamamlanmış emirlerini getirir."
    )
    @ApiResponse(responseCode = "200", description = "Tamamlanmış emirler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Tamamlanmış emir bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<TradeOrder>>> getSettledOrders(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(
            summary = "İptal Edilmiş Emirleri Getir",
            description = "Kullanıcının iptal edilmiş emirlerini getirir."
    )
    @ApiResponse(responseCode = "200", description = "İptal edilmiş emirler başarıyla listelendi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "İptal edilmiş emir bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    ResponseEntity<Response<List<TradeOrder>>> getCancelledOrders(@AuthenticationPrincipal UserDetails userDetails);

    @Operation(
            summary = "Emri İptal Et",
            description = "Belirtilen emir ID'sine sahip emri iptal eder."
    )
    @ApiResponse(responseCode = "200", description = "Emir başarıyla iptal edildi", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "404", description = "Emir bulunamadı", content = @Content(schema = @Schema(implementation = Response.class)))
    @ApiResponse(responseCode = "500", description = "Sunucu hatası", content = @Content(schema = @Schema(implementation = Response.class)))
    @Parameter(name = "orderId", description = "İptal edilecek emir ID'si", required = true)
    ResponseEntity<Response<TradeOrder>> cancelOrder(@Parameter Long orderId, @Parameter UserDetails userDetails);

    }