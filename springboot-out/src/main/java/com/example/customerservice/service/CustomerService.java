package com.example.customerservice.service;

import com.example.customerservice.dto.CustomerCreateRequest;
import com.example.customerservice.dto.CustomerResponse;
import com.example.customerservice.dto.CustomerUpdateRequest;
import com.example.customerservice.dto.OperationType;
import com.example.customerservice.exception.ApiException;
import com.example.customerservice.model.Customer;
import com.example.customerservice.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.objectMapper = objectMapper;
    }

    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        if (request == null) {
            throw new ApiException("Payload required for CREATE", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(request.getName()) || !StringUtils.hasText(request.getEmail())) {
             throw new ApiException("Name and Email are required for CREATE", HttpStatus.BAD_REQUEST);
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setStatus(request.getStatus() != null ? request.getStatus() : "ACTIVE"); // Default status
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Customer created with ID: {}", savedCustomer.getId());
        return convertToResponse(savedCustomer);
    }

    public CustomerResponse getCustomerById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ApiException("Customer ID is required for READ", HttpStatus.BAD_REQUEST);
        }
        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isEmpty()) {
            throw new ApiException("Customer not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        return convertToResponse(customerOptional.get());
    }

    public CustomerResponse updateCustomer(String id, CustomerUpdateRequest request) {
        if (!StringUtils.hasText(id)) {
            throw new ApiException("Customer ID is required for UPDATE", HttpStatus.BAD_REQUEST);
        }
        if (request == null) {
             throw new ApiException("Payload required for UPDATE", HttpStatus.BAD_REQUEST);
        }

        Optional<Customer> customerOptional = customerRepository.findById(id);
        if (customerOptional.isEmpty()) {
            throw new ApiException("Customer not found with ID: " + id, HttpStatus.NOT_FOUND);
        }

        Customer customer = customerOptional.get();
        if (StringUtils.hasText(request.getName())) {
            customer.setName(request.getName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            customer.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getStatus())) {
            customer.setStatus(request.getStatus());
        }
        customer.setUpdatedAt(LocalDateTime.now());

        Customer updatedCustomer = customerRepository.save(customer);
        logger.info("Customer updated with ID: {}", updatedCustomer.getId());
        return convertToResponse(updatedCustomer);
    }

    public void deleteCustomer(String id) {
        if (!StringUtils.hasText(id)) {
            throw new ApiException("Customer ID is required for DELETE", HttpStatus.BAD_REQUEST);
        }
        if (!customerRepository.existsById(id)) {
            throw new ApiException("Customer not found with ID: " + id, HttpStatus.NOT_FOUND);
        }
        customerRepository.deleteById(id);
        logger.info("Customer deleted with ID: {}", id);
    }

    public List<CustomerResponse> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private CustomerResponse convertToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setName(customer.getName());
        response.setEmail(customer.getEmail());
        response.setStatus(customer.getStatus());
        // Format dates to String as per COBOL RESPONSE-CTX.cpy RESP-BODY expectation
        response.setCreatedAt(customer.getCreatedAt() != null ? customer.getCreatedAt().toString() : null);
        response.setUpdatedAt(customer.getUpdatedAt() != null ? customer.getUpdatedAt().toString() : null);
        return response;
    }

    // Method to handle operations based on RequestContext, mimicking COBOL logic
    public ResponseContext processRequest(RequestContext requestContext) {
        ResponseContext responseContext = new ResponseContext();
        OperationType operation = requestContext.getOperation();

        try {
            switch (operation) {
                case CREATE:
                    CustomerCreateRequest createRequest = objectMapper.readValue(requestContext.getPayload(), CustomerCreateRequest.class);
                    CustomerResponse created = createCustomer(createRequest);
                    responseContext.setCode(HttpStatus.CREATED.value());
                    responseContext.setMessage("Customer created successfully.");
                    responseContext.setBody(objectMapper.writeValueAsString(created));
                    break;
                case READ:
                    CustomerResponse readResponse = getCustomerById(requestContext.getCustomerId());
                    responseContext.setCode(HttpStatus.OK.value());
                    responseContext.setMessage("Customer retrieved successfully.");
                    responseContext.setBody(objectMapper.writeValueAsString(readResponse));
                    break;
                case UPDATE:
                    CustomerUpdateRequest updateRequest = objectMapper.readValue(requestContext.getPayload(), CustomerUpdateRequest.class);
                    CustomerResponse updated = updateCustomer(requestContext.getCustomerId(), updateRequest);
                    responseContext.setCode(HttpStatus.OK.value());
                    responseContext.setMessage("Customer updated successfully.");
                    responseContext.setBody(objectMapper.writeValueAsString(updated));
                    break;
                case DELETE:
                    deleteCustomer(requestContext.getCustomerId());
                    responseContext.setCode(HttpStatus.NO_CONTENT.value());
                    responseContext.setMessage("Customer deleted successfully.");
                    responseContext.setBody(""); // No content for delete
                    break;
                case LIST:
                    List<CustomerResponse> customerList = getAllCustomers();
                    responseContext.setCode(HttpStatus.OK.value());
                    responseContext.setMessage("Customers retrieved successfully.");
                    responseContext.setBody(objectMapper.writeValueAsString(customerList));
                    break;
                default:
                    throw new ApiException("Unsupported operation: " + operation, HttpStatus.BAD_REQUEST);
            }
        } catch (ApiException e) {
            responseContext.setCode(e.getHttpStatus().value());
            responseContext.setMessage(e.getMessage());
            responseContext.setBody("");
            logger.error("API Exception during operation {}: {}", operation, e.getMessage(), e);
        } catch (Exception e) {
            responseContext.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseContext.setMessage("An internal server error occurred.");
            responseContext.setBody("");
            logger.error("Unexpected error during operation {}: {}", operation, e.getMessage(), e);
        }
        return responseContext;
    }
}
