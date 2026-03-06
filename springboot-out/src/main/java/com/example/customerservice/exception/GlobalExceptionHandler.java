package com.example.customerservice.exception;

import com.example.customerservice.dto.ResponseContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex, WebRequest request) {
        ResponseContext responseContext = new ResponseContext();
        responseContext.setCode(ex.getHttpStatus().value());
        responseContext.setMessage(ex.getMessage());
        responseContext.setBody(""); // API exceptions typically don't have a body in this context

        logger.error("API Exception: Code={}, Message={}, Path={}", ex.getHttpStatus().value(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(responseContext, ex.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        ResponseContext responseContext = new ResponseContext();
        responseContext.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        responseContext.setMessage("An unexpected error occurred.");
        responseContext.setBody("");

        logger.error("Generic Exception: Message={}, Path={}", ex.getMessage(), request.getDescription(false), ex);
        return new ResponseEntity<>(responseContext, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Helper to convert JSON string to ResponseContext for logging if needed
    private ResponseContext parseResponseBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(body, ResponseContext.class);
        } catch (IOException e) {
            logger.warn("Could not parse response body: {}", body, e);
            return null;
        }
    }
}
