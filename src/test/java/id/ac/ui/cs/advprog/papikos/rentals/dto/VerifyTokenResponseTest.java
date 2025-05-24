package id.ac.ui.cs.advprog.papikos.rentals.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VerifyTokenResponseTest {

    @Test
    void testVerifyTokenResponseData_SettersAndGetters() {
        VerifyTokenResponse.Data data = new VerifyTokenResponse.Data();
        String userId = "user123";
        String email = "test@example.com";
        String role = "TENANT";
        String status = "ACTIVE";

        data.setUserId(userId);
        data.setEmail(email);
        data.setRole(role);
        data.setStatus(status);

        assertEquals(userId, data.getUserId());
        assertEquals(email, data.getEmail());
        assertEquals(role, data.getRole());
        assertEquals(status, data.getStatus());
    }

    @Test
    void testVerifyTokenResponseData_NoArgsConstructor() {
        VerifyTokenResponse.Data data = new VerifyTokenResponse.Data();
        assertNull(data.getUserId());
        assertNull(data.getEmail());
        assertNull(data.getRole());
        assertNull(data.getStatus());
    }

    @Test
    void testVerifyTokenResponseData_AllArgsConstructor() {
        String userId = "user123";
        String email = "test@example.com";
        String role = "TENANT";
        String status = "ACTIVE";
        VerifyTokenResponse.Data data = new VerifyTokenResponse.Data(userId, email, role, status);

        assertEquals(userId, data.getUserId());
        assertEquals(email, data.getEmail());
        assertEquals(role, data.getRole());
        assertEquals(status, data.getStatus());
    }

    @Test
    void testVerifyTokenResponseData_Builder() {
        String userId = "user123";
        String email = "test@example.com";
        String role = "TENANT";
        String status = "ACTIVE";
        VerifyTokenResponse.Data data = VerifyTokenResponse.Data.builder()
                .userId(userId)
                .email(email)
                .role(role)
                .status(status)
                .build();

        assertEquals(userId, data.getUserId());
        assertEquals(email, data.getEmail());
        assertEquals(role, data.getRole());
        assertEquals(status, data.getStatus());
    }

    @Test
    void testVerifyTokenResponse_SettersAndGetters() {
        VerifyTokenResponse response = new VerifyTokenResponse();
        int status = 200;
        String message = "Success";
        VerifyTokenResponse.Data data = VerifyTokenResponse.Data.builder().userId("testUser").build();
        long timestamp = System.currentTimeMillis();

        response.setStatus(status);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(timestamp);

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void testVerifyTokenResponse_NoArgsConstructor() {
        VerifyTokenResponse response = new VerifyTokenResponse();
        assertEquals(0, response.getStatus());
        assertNull(response.getMessage());
        assertNull(response.getData());
        assertEquals(0, response.getTimestamp()); // Default long value
    }

    @Test
    void testVerifyTokenResponse_AllArgsConstructor() {
        int status = 200;
        String message = "Success";
        VerifyTokenResponse.Data data = VerifyTokenResponse.Data.builder().userId("testUser").build();
        long timestamp = System.currentTimeMillis();
        VerifyTokenResponse response = new VerifyTokenResponse(status, message, data, timestamp);

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void testVerifyTokenResponse_Builder() {
        int status = 200;
        String message = "Success";
        VerifyTokenResponse.Data data = VerifyTokenResponse.Data.builder().userId("testUser").build();
        long timestamp = System.currentTimeMillis();
        VerifyTokenResponse response = VerifyTokenResponse.builder()
                .status(status)
                .message(message)
                .data(data)
                .timestamp(timestamp)
                .build();

        assertEquals(status, response.getStatus());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertEquals(timestamp, response.getTimestamp());
    }
}