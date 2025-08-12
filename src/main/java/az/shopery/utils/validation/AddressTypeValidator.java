package az.shopery.utils.validation;

import az.shopery.utils.annotation.ValidAddressType;
import az.shopery.utils.enums.AddressType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AddressTypeValidator implements ConstraintValidator<ValidAddressType, AddressType> {

    private Set<AddressType> allowedValues;

    @Override
    public void initialize(ValidAddressType constraintAnnotation) {
        if (constraintAnnotation.anyOf().length > 0) {
            this.allowedValues = Arrays.stream(constraintAnnotation.anyOf()).collect(Collectors.toSet());
        }
    }

    @Override
    public boolean isValid(AddressType value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        if (allowedValues != null && !allowedValues.isEmpty()) {
            return allowedValues.contains(value);
        }
        return true;
    }
}
