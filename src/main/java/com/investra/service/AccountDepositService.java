package com.investra.service;

import com.investra.dtos.request.DepositRequest;
import com.investra.dtos.response.DepositResponse;
import com.investra.dtos.response.Response;

public interface AccountDepositService {

    /**
     * Hesaba bakiye yükleme işlemi gerçekleştirir
     * @param request Bakiye yükleme isteği
     * @param userEmail İşlemi yapan kullanıcının e-posta adresi
     * @return İşlem sonucu
     */
    Response<DepositResponse> depositToAccount(DepositRequest request, String userEmail);
}
