package com.nhom33.quanlychungcu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception được throw khi request không hợp lệ về mặt nghiệp vụ.
 * Ví dụ: Đăng ký tạm vắng cho nhân khẩu đã chuyển đi.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    
    public BadRequestException(String message) {
        super(message);
    }
    
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
