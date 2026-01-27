package az.shopery.utils.validation;

import az.shopery.utils.annotation.ValidPhone;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        if (Objects.isNull(phoneNumber) || phoneNumber.trim().isEmpty()) {
            return true;
        }

        try {
           PhoneNumber number = phoneNumberUtil.parse(phoneNumber, null);
           return phoneNumberUtil.isValidNumber(number);
        } catch (Exception e) {
            return false;
        }
    }
}
