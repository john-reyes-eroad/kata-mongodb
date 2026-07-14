package com.example.mongocrud.common;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
        body.put("fields", ex.getBindingResult().getFieldErrors().stream().map(fieldError -> {
            Map<String, String> field = new LinkedHashMap<>();
            field.put("field", fieldError.getField());
            field.put("message", fieldError.getDefaultMessage());
            return field;
        }).toList());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MongoWriteException.class)
    public ResponseEntity<Map<String, Object>> handleMongoWriteException(MongoWriteException ex) {
        if (ex.getError() == null || ex.getError().getCategory() != ErrorCategory.DUPLICATE_KEY) {
            throw ex;
        }
        String lowerMessage = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        String message = lowerMessage.contains("vin")
                ? "Vehicle VIN must be unique"
                : lowerMessage.contains("licensenumber")
                        ? "Driver license number must be unique"
                        : lowerMessage.contains("name")
                                ? "Driver name must be unique"
                                : "Unique constraint violation";
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error("conflict", message));
    }

    private Map<String, Object> error(String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        return body;
    }
}
