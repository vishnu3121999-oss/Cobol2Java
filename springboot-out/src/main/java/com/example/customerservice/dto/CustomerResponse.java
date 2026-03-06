package com.example.customerservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerResponse {
    private String id;
    private String name;
    private String email;
    private String status;
    private String createdAt;
    private String updatedAt;
}
