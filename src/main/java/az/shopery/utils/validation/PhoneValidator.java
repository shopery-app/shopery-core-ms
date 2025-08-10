package az.shopery.utils.validation;

import az.shopery.utils.annotation.ValidPhone;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true;
        }

        try {
           Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, null);
           return phoneNumberUtil.isValidNumber(number);
        } catch (Exception e) {
            return false;
        }
    }
}
