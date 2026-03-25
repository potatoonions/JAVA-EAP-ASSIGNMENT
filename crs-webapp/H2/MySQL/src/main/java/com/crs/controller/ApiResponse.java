package com.crs.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record ApiResponse<T>(int status, String message, T data) {

    public static <T> ResponseEntity<ApiResponse<T>> ok(String msg, T data) {
        return ResponseEntity.ok(new ApiResponse<>(200, msg, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String msg, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse<>(201, msg, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(400, msg, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ApiResponse<>(401, msg, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> forbidden(String msg) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ApiResponse<>(403, msg, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ApiResponse<>(404, msg, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> conflict(String msg) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ApiResponse<>(409, msg, null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(String msg) {
        return ResponseEntity.internalServerError()
            .body(new ApiResponse<>(500, msg, null));
    }
}
