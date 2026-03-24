package com.crs.controller;

public class ApiResponse<T> {

    private final int statusCode;
    private final String message;
    private final T data;

    private ApiResponse(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    /* Factory Methods */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null);
    }

    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409, message, null);
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /* Getters */
    public int getStatusCode() {
        return statusCode;
    }
    public String getMessage() {
        return message;
    }
    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("ApiResponse{statusCode=%d, message='%s', data=%s}", statusCode, message, data);
    }
}