package vn.edu.jwtjjwt.user;

import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }
    @GetMapping("/me") public Map<String, Object> me(Authentication auth) {
        var user = users.findByUsername(auth.getName()).orElseThrow();
        return Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole());
    }
    @GetMapping public List<Map<String, Object>> all() {
        return users.findAll().stream().map(u -> Map.<String, Object>of("id", u.getId(), "username", u.getUsername(), "role", u.getRole())).toList();
    }
}
