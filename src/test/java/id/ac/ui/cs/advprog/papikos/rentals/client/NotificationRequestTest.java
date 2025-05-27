package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class NotificationRequestTest {

    @Test
    void testNotificationRequestConstructorAndGetters() {
        UUID rentalId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        String title = "Test Title";
        String message = "Test Message";

        NotificationRequest request = new NotificationRequest(rentalId, recipientId, propertyId, title, message);

        assertEquals(rentalId, request.getRelatedRentalId());
        assertEquals(recipientId, request.getRecipientId());
        assertEquals(propertyId, request.getRelatedPropertyId());
        assertEquals(title, request.getTitle());
        assertEquals(message, request.getMessage());
    }

    @Test
    void testNotificationRequestSetters() {
        NotificationRequest request = new NotificationRequest();

        UUID rentalId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        UUID propertyId = UUID.randomUUID();
        String title = "New Title";
        String message = "New Message";

        request.setRelatedRentalId(rentalId);
        request.setRecipientId(recipientId);
        request.setRelatedPropertyId(propertyId);
        request.setTitle(title);
        request.setMessage(message);

        assertEquals(rentalId, request.getRelatedRentalId());
        assertEquals(recipientId, request.getRecipientId());
        assertEquals(propertyId, request.getRelatedPropertyId());
        assertEquals(title, request.getTitle());
        assertEquals(message, request.getMessage());
    }

    @Test
    void testNotificationRequestDefaultConstructor() {
        NotificationRequest request = new NotificationRequest();
        assertNull(request.getRelatedRentalId());
        assertNull(request.getRecipientId());
        assertNull(request.getRelatedPropertyId());
        assertNull(request.getTitle());
        assertNull(request.getMessage());
    }
}