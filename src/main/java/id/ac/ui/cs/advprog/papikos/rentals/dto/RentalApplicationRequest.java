package id.ac.ui.cs.advprog.papikos.rentals.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class RentalApplicationRequest {
    @NotNull
    private UUID kosId;

    @NotBlank
    private String submittedTenantName;

    @NotBlank
    private String submittedTenantPhone;

    @NotNull
    @FutureOrPresent
    private LocalDate rentalStartDate;

    @NotNull
    @Min(1)
    private Integer rentalDurationMonths;
}
