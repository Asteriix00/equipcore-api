package dev.asterix.equipcore_api.enumeration;

public enum ValidationErrorCode {

    EMAIL_REQUIRED,
    EMAIL_INVALID,
    PASSWORD_REQUIRED,
    UNKNOWN;

    public static ValidationErrorCode from(String message) {
        try {

            return valueOf(message);

        } catch (IllegalArgumentException exception) {

            return UNKNOWN;
        }
    }
}
