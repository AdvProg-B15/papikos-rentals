package id.ac.ui.cs.advprog.papikos.rentals.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RentalDto {
    private UUID rentalId;
    private UUID tenantUserId;
    private UUID kosId;
    private UUID ownerUserId;
    private String kosName;
    private String submittedTenantName;
    private String submittedTenantPhone;
    private LocalDate rentalStartDate;
    private int rentalDurationMonths;
    private LocalDate rentalEndDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}