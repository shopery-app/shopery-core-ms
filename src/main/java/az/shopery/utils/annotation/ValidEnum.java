package az.shopery.utils.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import az.shopery.utils.validation.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Constraint(validatedBy = EnumValidator.class)
@Target({ FIELD })
@Retention(RUNTIME)
public @interface ValidEnum {
    String message() default "Value is not valid!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    Class<? extends Enum<?>> enumClass();
    String[] excluded() default {};
}
