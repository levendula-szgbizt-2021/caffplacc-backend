package hu.bme.szgbizt.levendula.caffplacc.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Password {

    String message() default "Password has to be at least 8 characters long and must contain at least one number, an uppercase and a lowercase character.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
