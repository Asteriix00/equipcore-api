package dev.asterix.equipcore_api.dto.error;

import dev.asterix.equipcore_api.enumeration.ErrorCode;

public record ErrorResponse(
        String message,
        ErrorCode errorCode
) {
}
