package vidat.com.Online_Banking_system.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 409
    @ExceptionHandler({IllegalStateException.class, InsufficientFundsException.class})
    public ResponseEntity<Map<String, String>> handleConflict(Exception ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 404 (unknown or foreign account)
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(Exception ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400 (@Valid failures)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst().orElse("Invalid request");
        return error(HttpStatus.BAD_REQUEST, msg);
    }

    // 401 (login failures; same message for unknown user and wrong password)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleBadLogin(Exception ex) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
    // 409 (optimistic/pessimistic lock conflict after retries)
    @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class)
    public ResponseEntity<Map<String, String>> handleConcurrency(Exception ex) {
        return error(HttpStatus.CONFLICT, "The account was modified concurrently, please retry");
    }

    // 403 (ownership check in the service layer)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(Exception ex) {
        return error(HttpStatus.FORBIDDEN, "Access denied");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("error", message != null ? message : status.getReasonPhrase()));
    }
}