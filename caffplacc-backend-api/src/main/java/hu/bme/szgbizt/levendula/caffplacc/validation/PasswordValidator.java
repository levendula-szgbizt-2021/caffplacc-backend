package hu.bme.szgbizt.levendula.caffplacc.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, Object> {

    @Override
    public void initialize(Password constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        // If there was no password set, it does not need to be updated and can be left null
        if (value == null) {
            return true;
        }
        // Check if the password is 8 characters or longer.
        if (value.toString().length() < 8) {
            return false;
        }
        // ReGex to check if a string contains uppercase, lowercase character & numeric value
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(value.toString());
        return (m.matches());
    }
}
