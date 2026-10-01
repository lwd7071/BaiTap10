package vn.edu.jwtnimbus.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtServiceTest {
    private static final String SECRET = "test-secret-key-at-least-32-bytes-for-hs256";
    private final UserDetails demoUser = User.withUsername("demo").password("unused").roles("USER").build();

    @Test void isTokenValidReturnsFalseForTamperedSignature() {
        JwtService service = new JwtServiceImpl(SECRET, 60_000);
        String[] pieces = service.generateToken(demoUser).split("\\.");
        char replacement = pieces[2].charAt(0) == 'A' ? 'B' : 'A';
        pieces[2] = replacement + pieces[2].substring(1);

        assertFalse(service.isTokenValid(String.join(".", pieces), demoUser));
    }

    @Test void isTokenValidReturnsFalseForExpiredToken() {
        JwtService service = new JwtServiceImpl(SECRET, -1_000);
        String expiredToken = service.generateToken(demoUser);

        assertFalse(service.isTokenValid(expiredToken, demoUser));
    }
}
