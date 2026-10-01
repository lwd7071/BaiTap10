package vn.edu.jwtnimbus.user;

public record UserResponse(Long id, String username, String role) {
    public static UserResponse from(UserAccount user) { return new UserResponse(user.getId(), user.getUsername(), user.getRole()); }
}
