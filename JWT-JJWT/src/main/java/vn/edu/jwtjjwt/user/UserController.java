package vn.edu.jwtjjwt.user;

import vn.edu.jwtjjwt.api.ApiResponse;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }
    @GetMapping("/me") public ApiResponse<UserResponse> me(Authentication auth) {
        var user = users.findByUsername(auth.getName()).orElseThrow();
        return ApiResponse.success("Lấy hồ sơ thành công", UserResponse.from(user));
    }
    @GetMapping public ApiResponse<List<UserResponse>> all() {
        var data = users.findAll().stream().map(UserResponse::from).toList();
        return ApiResponse.success("Lấy danh sách người dùng thành công", data);
    }
}
