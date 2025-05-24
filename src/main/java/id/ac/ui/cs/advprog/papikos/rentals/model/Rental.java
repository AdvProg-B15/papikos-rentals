package id.ac.ui.cs.advprog.papikos.rentals.model;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "rentals")
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "rental_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tenant_user_id", nullable = false)
    private UUID tenantUserId;

    @Column(name = "kos_id", nullable = false)
    private UUID kosId;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

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
    private RentalStatus status = RentalStatus.PENDING_APPROVAL;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        if (rentalStartDate == null || rentalDurationMonths == null || rentalDurationMonths <= 0) {
            this.rentalEndDate = null;
            return;
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        rentalEndDate = rentalStartDate.plusMonths(rentalDurationMonths);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (rentalStartDate == null || rentalDurationMonths == null || rentalDurationMonths <= 0) {
            this.rentalEndDate = null;
            return;
        }
        rentalEndDate = rentalStartDate.plusMonths(rentalDurationMonths);
    }
}