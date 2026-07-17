package com.example.mongodb.adapter.inbound.common;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.mongocrud.common.DuplicateResourceException;
import com.example.mongocrud.common.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error("not_found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = error("validation_error", "Request validation failed");
        body.put("fields", ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex) {
        Map<String, Object> body = error("validation_error", "Request validation failed");
        body.put("fields", List.of(fieldError(ex.getParameterName(), "Parameter is required")));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error("conflict", ex.getMessage()));
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        return body;
    }

    private Map<String, String> fieldError(String fieldName, String message) {
        Map<String, String> field = new LinkedHashMap<>();
        field.put("field", fieldName);
        field.put("message", message);
        return field;
    }
}
