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
public class SuccessResponse {

    private String message;
    private LocalDateTime timestamp;

    public static SuccessResponse of(String message) {
        return SuccessResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}