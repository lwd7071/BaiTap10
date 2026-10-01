package vn.edu.jwtnimbus.security;

import vn.edu.jwtnimbus.api.ApiError;
import vn.edu.jwtnimbus.api.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiAccessDeniedHandler.class);
    private final ObjectMapper mapper;
    public ApiAccessDeniedHandler(ObjectMapper mapper) { this.mapper = mapper; }
    @Override public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        log.warn("authorization rejected status=403");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), ApiResponse.failure("Không được phép truy cập", List.of(new ApiError("authorization", "Bạn không có quyền truy cập tài nguyên này"))));
    }
}
