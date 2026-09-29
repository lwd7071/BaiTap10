package vn.edu.jwtnimbus.security;

import java.util.Date;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(UserDetails user);
    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails user);
    Date getExpiration(String token);
}
