package vn.edu.jwtnimbus.api;

import java.util.List;

public record ApiResponse<T>(boolean success, String message, T data, List<ApiError> errors) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, List.of());
    }
    public static <T> ApiResponse<T> failure(String message, List<ApiError> errors) {
        return new ApiResponse<>(false, message, null, List.copyOf(errors));
    }
}
