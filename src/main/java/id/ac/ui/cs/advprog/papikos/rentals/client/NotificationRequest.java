package id.ac.ui.cs.advprog.papikos.rentals.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private UUID relatedRentalId;
    private UUID recipientId;
    private UUID relatedPropertyId; // Ini adalah Kos ID
    private String title;
    private String message;
}