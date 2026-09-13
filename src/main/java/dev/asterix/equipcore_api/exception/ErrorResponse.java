package dev.asterix.equipcore_api.exception;

import dev.asterix.equipcore_api.enumeration.ErrorCode;

public record ErrorResponse(
        String message,
        ErrorCode errorCode
) {
}
