package dev.asterix.equipcore_api.enumeration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class ValidationErrorCodeTest {

    @Test
    void from_withEmailRequiredMessage() {

        ValidationErrorCode validationErrorCode = ValidationErrorCode.from("EMAIL_REQUIRED");

        Assertions.assertEquals(ValidationErrorCode.EMAIL_REQUIRED, validationErrorCode);
    }

    @Test
    void from_withEmailInvalidMessage() {

        ValidationErrorCode validationErrorCode = ValidationErrorCode.from("EMAIL_INVALID");

        Assertions.assertEquals(ValidationErrorCode.EMAIL_INVALID, validationErrorCode);
    }

    @Test
    void from_withPasswordRequiredMessage() {

        ValidationErrorCode validationErrorCode = ValidationErrorCode.from("PASSWORD_REQUIRED");

        Assertions.assertEquals(ValidationErrorCode.PASSWORD_REQUIRED, validationErrorCode);
    }

    @Test
    void from_withUnknownMessage() {

        ValidationErrorCode validationErrorCode = ValidationErrorCode.from("MESSAGE");

        Assertions.assertEquals(ValidationErrorCode.UNKNOWN, validationErrorCode);
    }

}