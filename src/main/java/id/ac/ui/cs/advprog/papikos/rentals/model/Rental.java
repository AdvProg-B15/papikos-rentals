package id.ac.ui.cs.advprog.papikos.rentals.model;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rental_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_user_id", nullable = false)
    private UUID tenantUserId; // Store only the ID

    @Column(name = "property_id", nullable = false)
    private UUID propertyId; // Store only the ID

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId; // Store only the ID (denormalized)

    @Column(name = "submitted_tenant_name", nullable = false, length = 255)
    private String submittedTenantName;

    @Column(name = "submitted_tenant_phone", nullable = false, length = 20)
    private String submittedTenantPhone;

    @Column(name = "rental_start_date", nullable = false)
    private LocalDate rentalStartDate;

    @Column(name = "rental_duration_months", nullable = false)
    private Integer rentalDurationMonths;

    @Column(name = "rental_end_date", nullable = false)
    private LocalDate rentalEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RentalStatus status = RentalStatus.PENDING_APPROVAL; // Default status

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    /**
     * Calculates and sets the rental end date based on start date and duration.
     * Should be called before saving/updating if start date or duration changes.
     */
    public void calculateAndSetEndDate() {
        if (this.rentalStartDate != null && this.rentalDurationMonths != null && this.rentalDurationMonths > 0) {
            this.rentalEndDate = this.rentalStartDate.plusMonths(this.rentalDurationMonths).minusDays(1); // Often end date is inclusive last day
        } else {
            this.rentalEndDate = null;
        }
    }

    public Rental(UUID tenantUserId, UUID propertyId, UUID ownerUserId, String submittedTenantName, String submittedTenantPhone, LocalDate rentalStartDate, Integer rentalDurationMonths) {
        this.tenantUserId = tenantUserId;
        this.propertyId = propertyId;
        this.ownerUserId = ownerUserId; // Must be fetched/provided
        this.submittedTenantName = submittedTenantName;
        this.submittedTenantPhone = submittedTenantPhone;
        this.rentalStartDate = rentalStartDate;
        this.rentalDurationMonths = rentalDurationMonths;
        this.status = RentalStatus.PENDING_APPROVAL;
        calculateAndSetEndDate();
    }

}