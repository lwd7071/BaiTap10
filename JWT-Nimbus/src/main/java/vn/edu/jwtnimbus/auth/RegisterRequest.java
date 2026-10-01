package vn.edu.jwtnimbus.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import vn.edu.jwtnimbus.auth.validation.BcryptSafePassword;

public record RegisterRequest(
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 64, message = "Username phải dài từ 3 đến 30 ký tự, không tính khoảng trắng đầu/cuối")
    @Pattern(regexp = "^\\s*[A-Za-z0-9._]{3,30}\\s*$", message = "Username chỉ được chứa chữ ASCII, số, dấu chấm và gạch dưới")
    String username,
    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, max = 72, message = "Password phải dài từ 8 đến 72 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).+$", message = "Password phải có ít nhất một chữ cái và một chữ số")
    @BcryptSafePassword
    String password
) {}
