package com.example.system_backend.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuccessResponse<T> {

    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> SuccessResponse<T> success(T data) {
        return SuccessResponse.<T>builder()
                .message("Success")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> SuccessResponse<T> success(String message, T data) {
        return SuccessResponse.<T>builder()
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static SuccessResponse<Void> of(String message) {
        return SuccessResponse.<Void>builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}