package vn.edu.jwtnimbus.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import vn.edu.jwtnimbus.auth.validation.BcryptSafePassword;

public record LoginRequest(
    @NotBlank(message = "Username không được để trống")
    @Size(max = 30, message = "Username không được dài quá 30 ký tự")
    String username,
    @NotBlank(message = "Password không được để trống")
    @BcryptSafePassword
    String password
) {}
