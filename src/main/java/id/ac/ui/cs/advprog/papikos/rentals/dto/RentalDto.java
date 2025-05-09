package id.ac.ui.cs.advprog.papikos.rentals.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder // Useful for constructing DTOs in service/tests
public class RentalDto {
    private UUID rentalId;
    private UUID tenantUserId;
    private UUID propertyId;
    private UUID ownerUserId;
    private String propertyName; // Added for better UI display
    private String submittedTenantName;
    private String submittedTenantPhone;
    private LocalDate rentalStartDate;
    private int rentalDurationMonths;
    private LocalDate rentalEndDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}