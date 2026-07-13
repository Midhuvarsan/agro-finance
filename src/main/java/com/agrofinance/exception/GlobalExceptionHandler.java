package com.agrofinance.exception;
 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
 
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
 
/**
 * @RestControllerAdvice = one class that intercepts exceptions from
 * EVERY controller and turns them into consistent JSON error responses,
 * instead of each controller (or Spring's default /error page — the
 * source of the confusing 403s we debugged in Phase 3) deciding
 * its own format.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
 
    /** Our own deliberate business errors (409 duplicate email, 404 not found, etc.). */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return build(status, ex.getReason(), null);
    }
 
    /** Bean Validation failures from @Valid — collects EVERY field error, not just the first. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }
 
    /** @PreAuthorize rejections — authenticated but lacking the required role. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action", null);
    }
 
    /**
     * Malformed/unparseable JSON request body. A client error, so 400 —
     * NOT the 500 it fell through to before this handler existed
     * (discovered via a PowerShell quoting artifact during Phase 4
     * testing that sent literal backslashes in the JSON).
     * Note: we don't echo Jackson's parse message — it can quote raw
     * request content back, same information-leak concern as the
     * catch-all handler.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body — expected valid JSON", null);
    }
 
    /**
     * Path variable type mismatch (e.g. /api/farmers/<9> where a Long
     * was expected) — also a client error, also spotted in the same
     * test session's logs falling through as an unhandled case.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "'", null);
    }
 
    /** Multipart size limit from Spring itself (server-level), before our service-level check. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException ex) {
        return build(HttpStatus.PAYLOAD_TOO_LARGE, "File exceeds the maximum allowed size", null);
    }
 
    /**
     * Last-resort catch-all. Deliberately returns a GENERIC message —
     * never ex.getMessage() — because internal exception text can leak
     * implementation details (SQL fragments, file paths, class names)
     * to clients. The real detail belongs in server logs, not responses.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
    }
 
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Map<String, String> fieldErrors) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
 
}
 




















