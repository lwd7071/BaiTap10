package vn.edu.jwtnimbus.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtServiceImpl implements JwtService {
    private final SecretKey key;
    private final long expirationMs;
    public JwtServiceImpl(@Value("${security.jwt.secret-key}") String secret, @Value("${security.jwt.expiration-time}") long expirationMs) {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"); this.expirationMs = expirationMs;
    }
    @Override public String generateToken(UserDetails user) {
        Date now = new Date();
        JWTClaimsSet claims = new JWTClaimsSet.Builder().subject(user.getUsername()).issueTime(now).expirationTime(new Date(now.getTime() + expirationMs)).build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try { jwt.sign(new MACSigner(key.getEncoded())); return jwt.serialize(); }
        catch (JOSEException e) { throw new IllegalStateException("Không thể ký JWT", e); }
    }
    @Override public String extractUsername(String token) {
        try { return parseAndVerify(token).getJWTClaimsSet().getSubject(); }
        catch (java.text.ParseException e) { throw new IllegalArgumentException("JWT không hợp lệ", e); }
    }
    @Override public boolean isTokenValid(String token, UserDetails user) { var jwt = parseAndVerify(token); try { return jwt.getJWTClaimsSet().getSubject().equals(user.getUsername()) && jwt.getJWTClaimsSet().getExpirationTime().after(new Date()); } catch (java.text.ParseException e) { return false; } }
    @Override public Date getExpiration(String token) { try { return parseAndVerify(token).getJWTClaimsSet().getExpirationTime(); } catch (java.text.ParseException e) { throw new IllegalArgumentException("JWT không hợp lệ", e); } }
    private SignedJWT parseAndVerify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(key.getEncoded()))) throw new IllegalArgumentException("Chữ ký JWT không hợp lệ");
            Date expiration = jwt.getJWTClaimsSet().getExpirationTime();
            if (expiration == null || !expiration.after(new Date())) throw new IllegalArgumentException("JWT đã hết hạn");
            return jwt;
        } catch (java.text.ParseException | JOSEException e) { throw new IllegalArgumentException("JWT không hợp lệ", e); }
    }
}
