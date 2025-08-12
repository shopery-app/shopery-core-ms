package az.shopery.utils.annotation;

import az.shopery.utils.enums.AddressType;
import az.shopery.utils.validation.AddressTypeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AddressTypeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAddressType {
    String message() default "Invalid location type provided.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    AddressType[] anyOf() default {};
}
