package com.lsm.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse_<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ResponseEntity<ApiResponse_<T>> httpError(HttpStatus s, String message) {
        return ResponseEntity.
                status(s).
                body(new ApiResponse_<>(
                        false,
                        message,
                        null
                ));
    }
}

