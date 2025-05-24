package id.ac.ui.cs.advprog.papikos.rentals.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ForbiddenExceptionTest {

    @Test
    void testForbiddenException() {
        String message = "User is forbidden";
        ForbiddenException exception = new ForbiddenException(message);
        assertEquals(message, exception.getMessage());
    }
}