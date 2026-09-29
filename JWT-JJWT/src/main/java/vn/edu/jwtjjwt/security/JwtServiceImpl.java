package vn.edu.jwtjjwt.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {
    private final SecretKey key;
    private final long expirationMs;
    public JwtServiceImpl(@Value("${security.jwt.secret-key}") String secret, @Value("${security.jwt.expiration-time}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirationMs = expirationMs;
    }
    @Override public String generateToken(UserDetails user) {
        Date now = new Date();
        return Jwts.builder().subject(user.getUsername()).issuedAt(now).expiration(new Date(now.getTime() + expirationMs)).signWith(key).compact();
    }
    @Override public String extractUsername(String token) { return claims(token).getSubject(); }
    @Override public boolean isTokenValid(String token, UserDetails user) { var claims = claims(token); return claims.getSubject().equals(user.getUsername()) && claims.getExpiration().after(new Date()); }
    @Override public Date getExpiration(String token) { return claims(token).getExpiration(); }
    private io.jsonwebtoken.Claims claims(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
