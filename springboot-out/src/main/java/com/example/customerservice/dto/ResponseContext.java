package com.example.customerservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResponseContext {
    private int code;
    private String message;
    private String body;
}
