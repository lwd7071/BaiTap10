package vn.edu.jwtnimbus.api;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException() { super("Tên đăng nhập đã tồn tại"); }
}
