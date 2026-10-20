package com.martini.fish_gen.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_FAILED = "Validation failed";

    public record ValidationError(String field, String message) {
    }

    // @Valid @RequestBody / @ModelAttribute
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> new ValidationError(
                        error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName(),
                        error.getDefaultMessage()))
                .toList();
        return validationProblem(errors);
    }

    // Constraints placed directly on controller method parameters, e.g. @PathVariable @Positive Long id
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        List<ValidationError> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ValidationError(
                                fieldName(error, result.getMethodParameter().getParameterName()),
                                error.getDefaultMessage())))
                .toList();
        return validationProblem(errors);
    }

    // @Validated beans outside the web layer, e.g. services
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ValidationError(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return validationProblem(errors);
    }

    // Malformed JSON or a value that cannot be converted, e.g. an unknown enum constant
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleHttpMessageNotReadable(HttpMessageNotReadableException ignoredEx) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request body is malformed or contains invalid values");
        problem.setTitle("Malformed request");
        return problem;
    }

    // Path variable or request param of the wrong type, e.g. /collections/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "the expected type";
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parameter '" + ex.getName() + "' must be of type " + requiredType);
        problem.setTitle("Invalid parameter");
        return problem;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        return problem;
    }

    // Failed login, e.g. unknown username or wrong password. Same message for both, so usernames can't be probed.
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ignoredEx) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        problem.setTitle("Authentication failed");
        return problem;
    }

    // 402 rather than 403: the user is allowed to generate, they just need to top up their credits first
    @ExceptionHandler(OutOfAiCreditsException.class)
    public ProblemDetail handleOutOfAiCredits(OutOfAiCreditsException ignoredEx) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.PAYMENT_REQUIRED,
                "Not enough AI credits to generate flashcards");
        problem.setTitle("Out of AI credits");
        return problem;
    }

    private static String fieldName(MessageSourceResolvable error, String parameterName) {
        return error instanceof FieldError fieldError ? fieldError.getField() : parameterName;
    }

    private static ProblemDetail validationProblem(List<ValidationError> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Request contains invalid fields");
        problem.setTitle(VALIDATION_FAILED);
        problem.setProperty("errors", errors);
        return problem;
    }
}
