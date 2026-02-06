package az.shopery.utils.validation;

import az.shopery.utils.annotation.ValidEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnumValidator implements ConstraintValidator<ValidEnum, Enum<?>> {

    private Set<String> allowedValues;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        Set<String> excluded = Set.of(constraintAnnotation.excluded());

        allowedValues = Stream.of(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .filter(v -> !excluded.contains(v))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        return Objects.nonNull(value) && allowedValues.contains(value.name());
    }
}
