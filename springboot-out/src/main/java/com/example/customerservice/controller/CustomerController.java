package com.example.customerservice.controller;

import com.example.customerservice.dto.OperationType;
import com.example.customerservice.dto.RequestContext;
import com.example.customerservice.dto.ResponseContext;
import com.example.customerservice.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);
    private final CustomerService customerService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CustomerController(CustomerService customerService, ObjectMapper objectMapper) {
        this.customerService = customerService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/process")
    public ResponseEntity<ResponseContext> processCustomerRequest(@RequestBody RequestContext requestContext) {
        logger.info("Received request: Operation={}, CustomerID={}, CorrelationID={}",
                requestContext.getOperation(), requestContext.getCustomerId(), requestContext.getCorrelationId());

        // Basic validation for operation
        if (requestContext.getOperation() == null) {
            ResponseContext errorResponse = new ResponseContext();
            errorResponse.setCode(HttpStatus.BAD_REQUEST.value());
            errorResponse.setMessage("Operation type is required.");
            errorResponse.setBody("");
            logger.warn("Missing operation in request. CorrelationID={}", requestContext.getCorrelationId());
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Map COBOL operations to enum
        OperationType operationType = OperationType.fromString(requestContext.getOperation().name());
        requestContext.setOperation(operationType); // Ensure enum is set

        if (operationType == OperationType.UNKNOWN) {
            ResponseContext errorResponse = new ResponseContext();
            errorResponse.setCode(HttpStatus.BAD_REQUEST.value());
            errorResponse.setMessage("Unsupported operation: " + requestContext.getOperation());
            errorResponse.setBody("");
            logger.warn("Unsupported operation '{}'. CorrelationID={}", requestContext.getOperation(), requestContext.getCorrelationId());
            return ResponseEntity.badRequest().body(errorResponse);
        }

        ResponseContext responseContext = customerService.processRequest(requestContext);

        HttpStatus httpStatus = HttpStatus.valueOf(responseContext.getCode());

        logger.info("Sending response: Code={}, Message={}, CorrelationID={}",
                responseContext.getCode(), responseContext.getMessage(), requestContext.getCorrelationId());

        return ResponseEntity.status(httpStatus).body(responseContext);
    }

    // Example of direct RESTful endpoints if needed, mapping to the processRequest logic
    // These are illustrative and the primary COBOL mapping is via the /process endpoint.

    @PostMapping("/create")
    public ResponseEntity<ResponseContext> createCustomerDirect(@RequestBody String payload) {
        RequestContext requestContext = new RequestContext();
        requestContext.setOperation(OperationType.CREATE);
        requestContext.setPayload(payload);
        requestContext.setCorrelationId("direct-create-" + System.currentTimeMillis()); // Example correlation ID
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.processRequest(requestContext));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseContext> getCustomerDirect(@PathVariable String id) {
        RequestContext requestContext = new RequestContext();
        requestContext.setOperation(OperationType.READ);
        requestContext.setCustomerId(id);
        requestContext.setCorrelationId("direct-read-" + System.currentTimeMillis()); // Example correlation ID
        return ResponseEntity.ok().body(customerService.processRequest(requestContext));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseContext> updateCustomerDirect(@PathVariable String id, @RequestBody String payload) {
        RequestContext requestContext = new RequestContext();
        requestContext.setOperation(OperationType.UPDATE);
        requestContext.setCustomerId(id);
        requestContext.setPayload(payload);
        requestContext.setCorrelationId("direct-update-" + System.currentTimeMillis()); // Example correlation ID
        return ResponseEntity.ok().body(customerService.processRequest(requestContext));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseContext> deleteCustomerDirect(@PathVariable String id) {
        RequestContext requestContext = new RequestContext();
        requestContext.setOperation(OperationType.DELETE);
        requestContext.setCustomerId(id);
        requestContext.setCorrelationId("direct-delete-" + System.currentTimeMillis()); // Example correlation ID
        return ResponseEntity.noContent().build(); // No body for DELETE
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseContext> getAllCustomersDirect() {
        RequestContext requestContext = new RequestContext();
        requestContext.setOperation(OperationType.LIST);
        requestContext.setCorrelationId("direct-list-" + System.currentTimeMillis()); // Example correlation ID
        return ResponseEntity.ok().body(customerService.processRequest(requestContext));
    }
}
