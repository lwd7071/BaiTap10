package vn.edu.jwtjjwt.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.nio.charset.StandardCharsets;

public class BcryptSafePasswordValidator implements ConstraintValidator<BcryptSafePassword, String> {
    @Override public boolean isValid(String password, ConstraintValidatorContext context) {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
