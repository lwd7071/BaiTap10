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
        log.warn("request rejected reason=input_validation status=400");
        List<ApiError> errors = exception.getBindingResult().getFieldErrors().stream()
            .map(error -> new ApiError(error.getField(), error.getDefaultMessage()))
            .distinct().toList();
        return ApiResponse.failure("Dữ liệu đầu vào không hợp lệ", errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiResponse<Void> unreadableBody() {
        log.warn("request rejected reason=unreadable_body status=400");
        return ApiResponse.failure("Request body không hợp lệ", List.of(new ApiError("request", "Không thể đọc dữ liệu JSON")));
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiResponse<Void> duplicateUsername(DuplicateUsernameException exception) {
        log.warn("request rejected reason=duplicate_username status=409");
        return ApiResponse.failure(exception.getMessage(), List.of(new ApiError("username", exception.getMessage())));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiResponse<Void> integrityConflict() {
        log.warn("request rejected reason=data_integrity_conflict status=409");
        String message = "Dữ liệu bị trùng hoặc vi phạm ràng buộc";
        return ApiResponse.failure(message, List.of(new ApiError("username", message)));
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ApiResponse<Void> invalidCredentials() {
        log.warn("request rejected reason=invalid_credentials status=401");
        return ApiResponse.failure("Tên đăng nhập hoặc mật khẩu không đúng", List.of(new ApiError("credentials", "Thông tin đăng nhập không hợp lệ")));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ApiResponse<Void> forbidden() {
        log.warn("request rejected reason=forbidden status=403");
        return ApiResponse.failure("Không được phép truy cập", List.of(new ApiError("authorization", "Bạn không có quyền truy cập tài nguyên này")));
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiResponse<Void> missingResource() {
        log.warn("request rejected reason=resource_not_found status=404");
        return ApiResponse.failure("Không tìm thấy tài nguyên", List.of(new ApiError("resource", "Tài nguyên không tồn tại")));
    }

    @ExceptionHandler(ResponseStatusException.class)
    org.springframework.http.ResponseEntity<ApiResponse<Void>> responseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        String message = status == HttpStatus.NOT_FOUND ? "Không tìm thấy tài nguyên"
            : status == HttpStatus.BAD_REQUEST ? "Yêu cầu không hợp lệ"
            : status == HttpStatus.UNAUTHORIZED ? "Chưa xác thực"
            : status == HttpStatus.FORBIDDEN ? "Không được phép truy cập"
            : "Yêu cầu không thể xử lý";
        if (exception.getStatusCode().is5xxServerError()) log.error("request failed status={}", exception.getStatusCode().value(), exception);
        else log.warn("request rejected status={}", exception.getStatusCode().value());
        return org.springframework.http.ResponseEntity.status(exception.getStatusCode()).body(ApiResponse.failure(message, List.of(new ApiError("request", message))));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ApiResponse<Void> unexpected(Exception exception) {
        log.error("Unhandled API error", exception);
        return ApiResponse.failure("Đã xảy ra lỗi máy chủ", List.of(new ApiError("server", "Vui lòng thử lại sau")));
    }
}
