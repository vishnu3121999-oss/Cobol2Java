package com.example.customerservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CustomerUpdateRequest {
    private String name;
    private String email;
    private String status;
}
