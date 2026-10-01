package vn.edu.jwtnimbus.api;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import io.swagger.v3.oas.annotations.Hidden;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Hidden
public class ApiErrorController implements ErrorController {
    private static final Logger log = LoggerFactory.getLogger(ApiErrorController.class);

    @RequestMapping("/error")
    ResponseEntity<ApiResponse<Void>> handleServletError(HttpServletRequest request) {
        Object statusAttribute = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttribute instanceof Integer value && value >= 400 && value <= 599 ? value : 500;
        boolean serverError = status >= 500;
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        if (serverError) log.error("servlet request failed status={}", status, exception);
        else log.warn("servlet request rejected status={}", status);
        String message = serverError ? "Đã xảy ra lỗi máy chủ" : "Yêu cầu không thể xử lý";
        String field = serverError ? "server" : "request";
        return ResponseEntity.status(status).body(ApiResponse.failure(message,
            List.of(new ApiError(field, serverError ? "Vui lòng thử lại sau" : message))));
    }
}
