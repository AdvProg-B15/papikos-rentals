package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestTest {

    @Test
    void testNotificationRequest_ConstructorAndGetters() {
        UUID recipientUserId = UUID.randomUUID();
        String type = "TEST_NOTIFICATION";
        String title = "Test Title";
        String message = "Test Message";
        UUID relatedRentalId = UUID.randomUUID();

        NotificationRequest request = new NotificationRequest(recipientUserId, type, title, message, relatedRentalId);

        assertEquals(recipientUserId, request.getRecipientUserId());
        assertEquals(type, request.getType());
        assertEquals(title, request.getTitle());
        assertEquals(message, request.getMessage());
        assertEquals(relatedRentalId, request.getRelatedRentalId());
    }
}