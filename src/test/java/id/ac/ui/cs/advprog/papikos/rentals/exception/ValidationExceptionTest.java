package id.ac.ui.cs.advprog.papikos.rentals.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {

    @Test
    void testValidationException() {
        String message = "Validation failed";
        ValidationException exception = new ValidationException(message);
        assertEquals(message, exception.getMessage());
    }
}