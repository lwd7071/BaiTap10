package vn.edu.jwtjjwt.api;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() { super("Tên đăng nhập đã tồn tại"); }
}
