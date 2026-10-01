package vn.edu.jwtnimbus.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtService jwtService;
    private final UserDetailsService users;
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService users) { this.jwtService = jwtService; this.users = users; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(7);
            String username = null;
            try {
                username = jwtService.extractUsername(token);
            } catch (RuntimeException invalidToken) {
                log.warn("bearer token rejected path={}", request.getRequestURI());
            }
            if (username != null) {
                var user = loadUserOrNull(username, request);
                boolean valid = false;
                if (user != null) {
                    try {
                        valid = jwtService.isTokenValid(token, user);
                    } catch (RuntimeException invalidToken) {
                        log.warn("bearer token rejected path={}", request.getRequestURI());
                    }
                }
                if (valid) {
                    var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }

    private org.springframework.security.core.userdetails.UserDetails loadUserOrNull(String username, HttpServletRequest request) {
        try {
            return users.loadUserByUsername(username);
        } catch (UsernameNotFoundException unknownUser) {
            log.warn("bearer token rejected path={}", request.getRequestURI());
            return null;
        }
    }
}
