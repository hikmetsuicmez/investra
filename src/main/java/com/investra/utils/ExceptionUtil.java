package com.investra.utils;

import com.investra.exception.*;

public class ExceptionUtil {
    private ExceptionUtil() {}

    public static ErrorCode getErrorCode(Exception e) {
        if (e instanceof ClientNotFoundException) {
            return ErrorCode.CLIENT_NOT_FOUND;
        } else if (e instanceof AccountNotFoundException) {
            return ErrorCode.ACCOUNT_NOT_FOUND;
        } else if (e instanceof UserNotFoundException) {
            return ErrorCode.USER_NOT_FOUND;
        } else if (e instanceof InvalidAmountException) {
            return ErrorCode.INVALID_AMOUNT;
        } else if (e instanceof AccountAlreadyExistsException) {
            return ErrorCode.ACCOUNT_ALREADY_EXISTS;
        } else if (e instanceof InvalidCredentialsException) {
            return ErrorCode.INVALID_CREDENTIALS;
        } else if (e instanceof NotificationException) {
            return ErrorCode.NOTIFICATION_ERROR;
        } else if (e instanceof ResourceNotFoundException) {
            return ErrorCode.RESOURCE_NOT_FOUND;
        } else if (e instanceof StockNotFoundException) {
            return ErrorCode.STOCK_NOT_FOUND;
        } else if (e instanceof InsufficientBalanceException) {
            return ErrorCode.INSUFFICIENT_FUNDS;
        } else if (e instanceof InsufficientStockException) {
            return ErrorCode.INSUFFICIENT_STOCK;
        } else if (e instanceof InactiveStockException) {
            return ErrorCode.INACTIVE_STOCK;
        } else if (e instanceof DatabaseOperationException) {
            return ErrorCode.DATABASE_ERROR;
        } else if (e instanceof CalculationException) {
            return ErrorCode.CALCULATION_ERROR;
        } else if (e instanceof BusinessException) {
            return ErrorCode.OPERATION_FAILED;
        } else if (e instanceof ValidationException) {
            return ErrorCode.VALIDATION_ERROR;
        } else if (e instanceof BadRequestException || e instanceof IllegalArgumentException) {
            return ErrorCode.INVALID_REQUEST;
        } else if (e instanceof InvalidOrExpiredTokenException) {
            return ErrorCode.INVALID_OR_EXPIRED_TOKEN;
        } else if (e instanceof BaseException) {
            // Eğer yeni bir ErrorCode eklenirse burada yakalanır
            return ((BaseException) e).getErrorCode();
        } else {
            return ErrorCode.UNEXPECTED_ERROR;
        }
    }
}
