package vn.edu.jwtjjwt.auth;

import vn.edu.jwtjjwt.security.JwtService;
import vn.edu.jwtjjwt.user.*;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    @PostMapping("/register") public Map<String, Object> register(@RequestBody Credentials body) {
        if (body.username() == null || body.username().isBlank() || body.password() == null || body.password().length() < 6)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên đăng nhập không được trống; mật khẩu cần ít nhất 6 ký tự");
        if (users.existsByUsername(body.username())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Tên đăng nhập đã tồn tại");
        var created = users.save(new UserAccount(body.username().trim(), encoder.encode(body.password()), "USER"));
        return Map.of("id", created.getId(), "username", created.getUsername(), "message", "Tạo tài khoản thành công");
    }
    @PostMapping("/login") public TokenResponse login(@RequestBody Credentials body) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(body.username(), body.password()));
        var user = userDetailsService.loadUserByUsername(body.username());
        String token = jwtService.generateToken(user);
        return new TokenResponse(token, "Bearer", jwtService.getExpiration(token).getTime());
    }
    public record Credentials(String username, String password) {}
    public record TokenResponse(String token, String tokenType, long expiresAt) {}
}
