package vn.edu.jwtjjwt.user;

import vn.edu.jwtjjwt.api.ApiResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Các API người dùng yêu cầu Bearer JWT")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }
    @GetMapping("/me") @Operation(summary = "Hồ sơ hiện tại") public ApiResponse<UserResponse> me(Authentication auth) {
        var user = users.findByUsername(auth.getName()).orElseThrow();
        return ApiResponse.success("Lấy hồ sơ thành công", UserResponse.from(user));
    }
    @GetMapping @Operation(summary = "Danh sách người dùng") public ApiResponse<List<UserResponse>> all() {
        var data = users.findAll().stream().map(UserResponse::from).toList();
        return ApiResponse.success("Lấy danh sách người dùng thành công", data);
    }
}
