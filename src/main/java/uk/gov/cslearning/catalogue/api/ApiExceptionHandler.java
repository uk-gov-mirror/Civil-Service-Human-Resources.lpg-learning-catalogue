package uk.gov.cslearning.catalogue.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.cslearning.catalogue.domain.ErrorDtoFactory;
import uk.gov.cslearning.catalogue.dto.ErrorDto;
import uk.gov.cslearning.catalogue.exception.*;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ApiExceptionHandler {

    private final ErrorDtoFactory errorDtoFactory;

    @ExceptionHandler({IllegalStateException.class})
    public ResponseEntity handleIllegalStateException(Exception e) {
        log.error("Bad Request: ", e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorDto> handleConstraintViolationExcetpion(ConstraintViolationException e) {
        log.error("Bad Request: ", e);
        List<String> errors = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).sorted().collect(Collectors.toList());
        ErrorDto error = new ErrorDto(errors, 400, "Validation error");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity handleResourceNotFoundException(ResourceNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        return errorDtoFactory.createWithErrorFields(HttpStatus.BAD_REQUEST, result.getFieldErrors()).getAsResponseEntity();
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDto> handleValidationException(ValidationException ex) {
        ErrorDto error = new ErrorDto(ex.getMessage(), 400, "Validation error");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorDto error = new ErrorDto(ex.getMessage(), 403, "Access is denied");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorDto> handleForbiddenException(ForbiddenException ex) {
        ErrorDto error = new ErrorDto(ex.getMessage(), 403, "Forbidden exception");
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(GenericServerException.class)
    public ResponseEntity<ErrorDto> handleServerException(GenericServerException ex) {
        ErrorDto error = new ErrorDto(ex.getMessage(), 500, "Server exception");
        return ResponseEntity.badRequest().body(error);
    }
}
