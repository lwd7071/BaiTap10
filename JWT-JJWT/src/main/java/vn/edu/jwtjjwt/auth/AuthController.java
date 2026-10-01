package vn.edu.jwtjjwt.auth;

import vn.edu.jwtjjwt.api.ApiResponse;
import vn.edu.jwtjjwt.api.DuplicateUsernameException;
import vn.edu.jwtjjwt.security.JwtService;
import vn.edu.jwtjjwt.user.UserRepository;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Đăng ký tài khoản và đăng nhập để nhận JWT")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService) {
        this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager; this.userDetailsService = userDetailsService; this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Đăng ký", description = "Username 3–30 ký tự; password 8–72 ký tự, có chữ và số.")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest body) {
        String username = UsernameNormalizer.normalize(body.username());
        if (users.existsByUsername(username)) throw new DuplicateUsernameException();
        var created = users.save(new vn.edu.jwtjjwt.user.UserAccount(username, encoder.encode(body.password()), "USER"));
        log.info("account registration completed");
        var data = new RegisterResponse(created.getId(), created.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo tài khoản thành công", data));
    }

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập", description = "Trả về Bearer JWT trong data.token khi thông tin hợp lệ.")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest body) {
        String username = UsernameNormalizer.normalize(body.username());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, body.password()));
        var user = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(user);
        log.info("authentication succeeded");
        var data = new TokenResponse(token, "Bearer", jwtService.getExpiration(token).getTime());
        return ApiResponse.success("Đăng nhập thành công", data);
    }

    public record RegisterResponse(Long id, String username) {}
    public record TokenResponse(String token, String tokenType, long expiresAt) {}
}
