package com.investra.service;

import com.investra.dtos.request.WithdrawalRequest;
import com.investra.dtos.response.WithdrawalResponse;
import com.investra.dtos.response.Response;

public interface AccountWithdrawalService {

    /**
     * Hesaptan bakiye çıkışı işlemi gerçekleştirir
     * @param request Bakiye çıkışı isteği
     * @param userEmail İşlemi yapan kullanıcının e-posta adresi
     * @return İşlem sonucu
     */
    Response<WithdrawalResponse> withdrawFromAccount(WithdrawalRequest request, String userEmail);
}
