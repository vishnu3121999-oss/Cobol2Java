package com.example.customerservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RequestContext {
    private OperationType operation;
    private String customerId;
    private String payload;
    private String correlationId;
}
