package dev.asterix.equipcore_api.exception;

import dev.asterix.equipcore_api.dto.error.ErrorResponse;
import dev.asterix.equipcore_api.dto.error.ValidationErrorResponse;
import dev.asterix.equipcore_api.enumeration.ErrorCode;
import dev.asterix.equipcore_api.enumeration.ValidationErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // System exceptions
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException() {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ErrorResponse(
                                "Email or password is incorrect",
                                ErrorCode.INVALID_CREDENTIALS
                        )
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        List<ValidationErrorCode> errorCodes = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> ValidationErrorCode.from(fieldError.getDefaultMessage()))
                .distinct()
                .toList();

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(
                        new ValidationErrorResponse(
                                "Validation failed",
                                ErrorCode.VALIDATION_ERROR,
                                errorCodes
                        )
                );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException() {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        new ErrorResponse(
                                "Request is malformed",
                                ErrorCode.MALFORMED_REQUEST
                        )
                );
    }
}
