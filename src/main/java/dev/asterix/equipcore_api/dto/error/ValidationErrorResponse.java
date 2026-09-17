package dev.asterix.equipcore_api.dto.error;

import dev.asterix.equipcore_api.enumeration.ErrorCode;
import dev.asterix.equipcore_api.enumeration.ValidationErrorCode;

import java.util.List;

public record ValidationErrorResponse(
        String message,
        ErrorCode errorCode,
        List<ValidationErrorCode> errorCodes
) {
}
