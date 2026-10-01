package vn.edu.jwtjjwt.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = BcryptSafePasswordValidator.class)
@Target({ FIELD, PARAMETER, ANNOTATION_TYPE, RECORD_COMPONENT })
@Retention(RUNTIME)
public @interface BcryptSafePassword {
    String message() default "Password vượt quá giới hạn 72 byte của BCrypt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
