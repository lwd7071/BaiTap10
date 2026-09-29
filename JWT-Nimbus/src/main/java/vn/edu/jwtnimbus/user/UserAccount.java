package vn.edu.jwtnimbus.user;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(name = "user_role", nullable = false, length = 30)
    private String role = "USER";
    protected UserAccount() {}
    public UserAccount(String username, String password, String role) { this.username = username; this.password = password; this.role = role; }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
}
