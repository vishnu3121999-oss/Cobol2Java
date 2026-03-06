```java
import java.util.Objects;

/**
 * Simulates a simple REST API endpoint handler, converting COBOL logic to Java.
 * This class manages request routing, customer data retrieval, and customer creation.
 */
public class RestApiHandler {

    // Represents the state of the current request/response cycle.
    private String requestMethod;
    private String requestPath;
    private String requestBody;
    private int responseStatus;
    private String responseBody;

    // Represents customer data, typically fetched from a data source.
    private int customerId;
    private String customerName;

    /**
     * Constructs a new RestApiHandler with default values.
     * Initializes with a sample customer.
     */
    public RestApiHandler() {
        // Default values mimicking COBOL working storage.
        this.requestMethod = "GET";
        this.requestPath = "/customers";
        this.requestBody = ""; // COBOL SPACES is typically an empty string in Java.
        this.responseStatus = 200;
        this.responseBody = "";

        // Sample customer data.
        this.customerId = 10001;
        this.customerName = "JOHN DOE";
    }

    /**
     * The main entry point for the simulated API processing.
     * It orchestrates the request routing and response printing.
     */
    public void processRequest() {
        routeRequest();
        displayResponse();
        // In Java, the program naturally ends when main() finishes.
        // COBOL's STOP RUN is implicitly handled.
    }

    /**
     * Determines the appropriate action based on the request method and path.
     * This method simulates a COBOL EVALUATE TRUE WHEN structure.
     */
    private void routeRequest() {
        if (Objects.equals("GET", requestMethod) && Objects.equals("/health", requestPath)) {
            handleHealthCheck();
        } else if (Objects.equals("GET", requestMethod) && Objects.equals("/customers", requestPath)) {
            handleGetCustomerList();
        } else if (Objects.equals("POST", requestMethod) && Objects.equals("/customers", requestPath)) {
            handleCreateCustomer();
        } else {
            handleNotFound();
        }
    }

    /**
     * Handles the /health endpoint request.
     */
    private void handleHealthCheck() {
        this.responseStatus = 200;
        this.responseBody = "{\"status\":\"UP\"}";
    }

    /**
     * Handles the GET /customers endpoint request.
     * Retrieves and formats customer data.
     */
    private void handleGetCustomerList() {
        this.responseStatus = 200;

        // Using StringBuilder for efficient string concatenation, idiomatic in Java.
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("{\"customers\":[{");
        responseBuilder.append("\"id\":").append(this.customerId);
        responseBuilder.append(",\"name\":\"").append(this.customerName).append("\"");
        responseBuilder.append("}]}");
        this.responseBody = responseBuilder.toString();
    }

    /**
     * Handles the POST /customers endpoint request.
     * Simulates customer creation.
     */
    private void handleCreateCustomer() {
        this.responseStatus = 201; // Created
        this.responseBody = "{\"message\":\"Customer created\"}";
        // In a real application, you would parse the requestBody here to get customer details
        // and then potentially update a database or data structure.
        // For this conversion, we are preserving the original logic which doesn't use requestBody.
    }

    /**
     * Handles requests for unknown paths or methods.
     */
    private void handleNotFound() {
        this.responseStatus = 404; // Not Found
        this.responseBody = "{\"error\":\"Not Found\"}";
    }

    /**
     * Displays the HTTP status and response body to the console.
     * This is the Java equivalent of COBOL's DISPLAY statement.
     */
    private void displayResponse() {
        System.out.println("HTTP STATUS: " + this.responseStatus);
        System.out.println("BODY: " + this.responseBody);
    }

    // --- Getters and Setters ---
    // These provide controlled access to the handler's state, promoting encapsulation.

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Main method to demonstrate the RestApiHandler.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        RestApiHandler handler = new RestApiHandler();

        // Example 1: GET /customers
        System.out.println("--- Processing GET /customers ---");
        handler.setRequestMethod("GET");
        handler.setRequestPath("/customers");
        handler.processRequest();
        System.out.println();

        // Example 2: POST /customers
        System.out.println("--- Processing POST /customers ---");
        handler.setRequestMethod("POST");
        handler.setRequestPath("/customers");
        handler.setRequestBody("{\"name\":\"Jane Doe\"}"); // Example body, not used by current logic
        handler.processRequest();
        System.out.println();

        // Example 3: GET /health
        System.out.println("--- Processing GET /health ---");
        handler.setRequestMethod("GET");
        handler.setRequestPath("/health");
        handler.processRequest();
        System.out.println();

        // Example 4: Unknown path
        System.out.println("--- Processing GET /orders ---");
        handler.setRequestMethod("GET");
        handler.setRequestPath("/orders");
        handler.processRequest();
        System.out.println();
    }
}
```