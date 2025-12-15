package az.shopery.handler;

import az.shopery.handler.exception.AddressLimitExceededException;
import az.shopery.handler.exception.CooldownNotMetException;
import az.shopery.handler.exception.EmailAlreadyExistsException;
import az.shopery.handler.exception.ExternalServiceException;
import az.shopery.handler.exception.FileStorageException;
import az.shopery.handler.exception.IllegalRequestException;
import az.shopery.handler.exception.InvalidCredentialsException;
import az.shopery.handler.exception.InvalidUuidFormatException;
import az.shopery.handler.exception.JwtAuthenticationException;
import az.shopery.handler.exception.OwnProductInteractionException;
import az.shopery.handler.exception.ResourceNotFoundException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.HtmlUtils;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                                                                         HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex,
                                                                           HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({InvalidCredentialsException.class, JwtAuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(Exception ex,
                                                                        HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex,
                                                                     HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex,
                                                                               HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(IllegalRequestException.class)
    public ResponseEntity<ErrorResponse> handleIllegalRequestException(IllegalRequestException ex,
                                                                       HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(InvalidUuidFormatException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUuidFormatException(InvalidUuidFormatException ex,
                                                                          HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(CooldownNotMetException.class)
    public ResponseEntity<ErrorResponse> handleCooldownNotMetException(CooldownNotMetException ex,
                                                                       HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.TOO_MANY_REQUESTS, request);
    }

    @ExceptionHandler(AddressLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleAddressLimitExceededException(AddressLimitExceededException ex,
                                                                             HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(OwnProductInteractionException.class)
    public ResponseEntity<ErrorResponse> handleOwnProductInteractionException(OwnProductInteractionException ex,
                                                                              HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ErrorResponse> handleFileStorageException(FileStorageException ex,
                                                                    HttpServletRequest request) {
        if (ex.getMessage().contains("empty file")) {
            return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
        }
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalServiceException(ExternalServiceException ex,
                                                                        HttpServletRequest request) {
        log.error("External service error: {}", ex.getMessage());
        return buildErrorResponse(ex, HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        String sanitizedPath = HtmlUtils.htmlEscape(request.getRequestURI());
        body.put("path", sanitizedPath);

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = error.getDefaultMessage();
            String messageToSanitize = (message != null) ? message : "Invalid value.";
            String sanitizedMessage = HtmlUtils.htmlEscape(messageToSanitize);
            errors.put(error.getField(), sanitizedMessage);
        });
        body.put("errors", errors);

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimitException(RequestNotPermitted ex,
                                                                  HttpServletRequest request) {
        String message = "Rate limit exceeded, please try again later.";
        Exception customEx = new RuntimeException(message, ex);
        return buildErrorResponse(customEx, HttpStatus.TOO_MANY_REQUESTS, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex,
                                                                HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                        HttpServletRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex, HttpStatus status, HttpServletRequest request) {
        String sanitizedMessage = HtmlUtils.htmlEscape(ex.getMessage());
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .statusCode(status.value())
                .timestamp(LocalDateTime.now())
                .message(sanitizedMessage)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(errorResponse, status);
    }
}
