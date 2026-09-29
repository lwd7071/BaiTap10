package vn.edu.jwtnimbus.security;

import vn.edu.jwtnimbus.user.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository users;
    public UserDetailsServiceImpl(UserRepository users) { this.users = users; }
    @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản"));
        return User.withUsername(user.getUsername()).password(user.getPassword()).roles(user.getRole()).build();
    }
}
