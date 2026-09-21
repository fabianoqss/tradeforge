package com.fabiano.tradeforge.controllers.handlers;

import com.fabiano.tradeforge.services.exceptions.AssetNotTradableException;
import com.fabiano.tradeforge.services.exceptions.InsufficientBalanceException;
import com.fabiano.tradeforge.services.exceptions.InsufficientPositionException;
import com.fabiano.tradeforge.services.exceptions.QuoteUnavailableException;
import com.fabiano.tradeforge.services.exceptions.ResourceNotFoundException;
import com.fabiano.tradeforge.services.exceptions.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<StandardError> userAlreadyExists(UserAlreadyExistsException userAlreadyExistsException, HttpServletRequest request ) {
        HttpStatus status = HttpStatus.CONFLICT;
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(status.value());
        standardError.setError("User Already Exists");
        standardError.setMessage(userAlreadyExistsException.getMessage());
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(standardError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> resourceNotFound(ResourceNotFoundException resourceNotFoundException, HttpServletRequest request ) {
        HttpStatus status = HttpStatus.CONFLICT;
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(status.value());
        standardError.setError("User doesn't exist");
        standardError.setMessage(resourceNotFoundException.getMessage());
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(standardError);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<StandardError> insufficientBalance(InsufficientBalanceException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(status.value());
        standardError.setError("Insufficient Balance");
        standardError.setMessage(e.getMessage());
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(standardError);
    }

    @ExceptionHandler(InsufficientPositionException.class)
    public ResponseEntity<StandardError> insufficientPosition(InsufficientPositionException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(status.value());
        standardError.setError("Insufficient Position");
        standardError.setMessage(e.getMessage());
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(standardError);
    }

    @ExceptionHandler(AssetNotTradableException.class)
    public ResponseEntity<StandardError> assetNotTradable(AssetNotTradableException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(status.value());
        standardError.setError("Asset Not Tradable");
        standardError.setMessage(e.getMessage());
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(standardError);
    }

    @ExceptionHandler(QuoteUnavailableException.class)
    public ResponseEntity<StandardError> quoteUnavailable(QuoteUnavailableException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(status.value());
        standardError.setError("Quote Unavailable");
        standardError.setMessage(e.getMessage());
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(standardError);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<StandardError> concurrentModification(ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        StandardError standardError = new StandardError();
        standardError.setTimestamp(Instant.now());
        standardError.setStatus(status.value());
        standardError.setError("Concurrent Modification");
        standardError.setMessage("This portfolio was modified by another request at the same time. Please retry.");
        standardError.setPath(request.getRequestURI());
        return ResponseEntity.status(status).body(standardError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        ValidationError err = new ValidationError();
        err.setTimestamp(Instant.now());
        err.setStatus(status.value());
        err.setError("Validation exception");
        err.setMessage("One or more fields are invalid");
        err.setPath(request.getRequestURI());

        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            err.addError(f.getField(), f.getDefaultMessage());
        }

        return ResponseEntity.status(status).body(err);
    }

}


