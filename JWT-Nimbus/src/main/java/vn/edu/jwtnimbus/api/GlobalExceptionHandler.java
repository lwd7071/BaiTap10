package vn.edu.jwtnimbus.api;

import java.util.List;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiResponse<Void> invalidFields(MethodArgumentNotValidException exception) {
        List<ApiError> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new ApiError(error.getField(), error.getDefaultMessage()))
            .distinct().toList();
        return ApiResponse.failure("Dữ liệu đầu vào không hợp lệ", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiResponse<Void> unreadableBody() {
        return ApiResponse.failure("Request body không hợp lệ", List.of(new ApiError("request", "Không thể đọc dữ liệu JSON")));
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiResponse<Void> duplicateUsername(DuplicateUsernameException exception) {
        return ApiResponse.failure(exception.getMessage(), List.of(new ApiError("username", exception.getMessage())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiResponse<Void> integrityConflict() {
        String message = "Dữ liệu bị trùng hoặc vi phạm ràng buộc";
        return ApiResponse.failure(message, List.of(new ApiError("username", message)));
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiResponse<Void> invalidCredentials() {
        return ApiResponse.failure("Tên đăng nhập hoặc mật khẩu không đúng", List.of(new ApiError("credentials", "Thông tin đăng nhập không hợp lệ")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiResponse<Void> forbidden() {
        return ApiResponse.failure("Không được phép truy cập", List.of(new ApiError("authorization", "Bạn không có quyền truy cập tài nguyên này")));
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiResponse<Void> missingResource() {
        return ApiResponse.failure("Không tìm thấy tài nguyên", List.of(new ApiError("resource", "Tài nguyên không tồn tại")));
    }

    @ExceptionHandler(ResponseStatusException.class)
    org.springframework.http.ResponseEntity<ApiResponse<Void>> responseStatus(ResponseStatusException exception) {
        String message = exception.getReason() == null ? exception.getStatusCode().toString() : exception.getReason();
        return org.springframework.http.ResponseEntity.status(exception.getStatusCode()).body(ApiResponse.failure(message, List.of(new ApiError("request", message))));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiResponse<Void> unexpected(Exception exception) {
        log.error("Unhandled API error", exception);
        return ApiResponse.failure("Đã xảy ra lỗi máy chủ", List.of(new ApiError("server", "Vui lòng thử lại sau")));
    }
}
