package id.ac.ui.cs.advprog.papikos.rentals.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpdateRentalSubmissionRequest {
    private String submittedTenantName;
    private String submittedTenantPhone;
    @FutureOrPresent
    private LocalDate rentalStartDate;
    @Min(1)
    private Integer rentalDurationMonths;
}
