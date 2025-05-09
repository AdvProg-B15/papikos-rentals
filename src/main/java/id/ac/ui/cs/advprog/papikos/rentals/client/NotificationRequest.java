package id.ac.ui.cs.advprog.papikos.rentals.client;

import lombok.Getter;

import java.util.UUID;

@Getter
public class NotificationRequest {
    private UUID recipientUserId;
    private String type; // e.g., RENTAL_APPLICATION_SUBMITTED
    private String title;
    private String message;
    private UUID relatedRentalId;

    public NotificationRequest(UUID recipientUserId, String type, String title, String message, UUID relatedRentalId) {
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedRentalId = relatedRentalId;
    }
}