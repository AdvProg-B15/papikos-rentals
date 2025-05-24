package id.ac.ui.cs.advprog.papikos.rentals.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testApiResponse_Builder_DefaultOk() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .data("Test Data")
                .build();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertNull(response.getMessage()); // Default message is null if not set
        assertEquals("Test Data", response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_WithStatusAndMessage() {
        ApiResponse<Integer> response = ApiResponse.<Integer>builder()
                .status(HttpStatus.CREATED)
                .message("Resource created")
                .data(123)
                .build();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals("Resource created", response.getMessage());
        assertEquals(123, response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_OkHelper() {
        ApiResponse<String> response = ApiResponse.<String>builder().ok("Success Data");

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("Success", response.getMessage());
        assertEquals("Success Data", response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_CreatedHelper() {
        ApiResponse<String> response = ApiResponse.<String>builder().created("Created Resource");

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals("Resource created successfully", response.getMessage());
        assertEquals("Created Resource", response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_BadRequestHelper() {
        ApiResponse<Object> response = ApiResponse.builder().badRequest("Invalid input");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Invalid input", response.getMessage());
        assertNull(response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_BadRequestHelperWithGenericType() {
        ApiResponse<String> response = ApiResponse.<String>builder().badRequest("Invalid input");

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        assertEquals("Invalid input", response.getMessage());
        assertNull(response.getData());
        assertTrue(response.getTimestamp() > 0);
    }


    @Test
    void testApiResponse_Builder_NotFoundHelper() {
        ApiResponse<Object> response = ApiResponse.builder().notFound("Resource not found");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Resource not found", response.getMessage());
        assertNull(response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_NotFoundHelperWithGenericType() {
        ApiResponse<String> response = ApiResponse.<String>builder().notFound("Resource not found");

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
        assertEquals("Resource not found", response.getMessage());
        assertNull(response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_InternalErrorHelper() {
        ApiResponse<Object> response = ApiResponse.builder().internalError("Internal server error");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("Internal server error", response.getMessage());
        assertNull(response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

    @Test
    void testApiResponse_Builder_InternalErrorHelperWithGenericType() {
        ApiResponse<String> response = ApiResponse.<String>builder().internalError("Internal server error");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());
        assertEquals("Internal server error", response.getMessage());
        assertNull(response.getData());
        assertTrue(response.getTimestamp() > 0);
    }

}