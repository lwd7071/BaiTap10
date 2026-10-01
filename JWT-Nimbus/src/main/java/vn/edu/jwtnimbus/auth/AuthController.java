package vn.edu.jwtnimbus.auth;

import vn.edu.jwtnimbus.api.ApiResponse;
import vn.edu.jwtnimbus.api.DuplicateUsernameException;
import vn.edu.jwtnimbus.security.JwtService;
import vn.edu.jwtnimbus.user.UserRepository;
import jakarta.validation.Valid;
import java.util.Date;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService) {
        this.users = users; this.encoder = encoder; this.authenticationManager = authenticationManager; this.userDetailsService = userDetailsService; this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest body) {
        String username = UsernameNormalizer.normalize(body.username());
        if (users.existsByUsername(username)) throw new DuplicateUsernameException();
        var created = users.save(new vn.edu.jwtnimbus.user.UserAccount(username, encoder.encode(body.password()), "USER"));
        var data = new RegisterResponse(created.getId(), created.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo tài khoản thành công", data));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest body) {
        String username = UsernameNormalizer.normalize(body.username());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, body.password()));
        var user = userDetailsService.loadUserByUsername(username);
        String token = jwtService.generateToken(user);
        var data = new TokenResponse(token, "Bearer", jwtService.getExpiration(token).getTime());
        return ApiResponse.success("Đăng nhập thành công", data);
    }

    public record RegisterResponse(Long id, String username) {}
    public record TokenResponse(String token, String tokenType, long expiresAt) {}
}
