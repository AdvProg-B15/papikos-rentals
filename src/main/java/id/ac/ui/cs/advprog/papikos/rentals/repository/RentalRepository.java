package id.ac.ui.cs.advprog.papikos.rentals.repository;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID>, JpaSpecificationExecutor<Rental> {

    // For GET /rentals/my
    List<Rental> findByTenantUserIdOrderByCreatedAtDesc(UUID tenantUserId);

    // For GET /rentals/owner (base case)
    List<Rental> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);

    // For GET /rentals/owner?status=...
    List<Rental> findByOwnerUserIdAndStatusOrderByCreatedAtDesc(UUID ownerUserId, RentalStatus status);

    // For GET /rentals/owner?propertyId=...
    List<Rental> findByOwnerUserIdAndPropertyIdOrderByCreatedAtDesc(UUID ownerUserId, UUID propertyId);

    // For GET /rentals/owner?status=...&propertyId=...
    List<Rental> findByOwnerUserIdAndPropertyIdAndStatusOrderByCreatedAtDesc(UUID ownerUserId, UUID propertyId, RentalStatus status);

    // For security checks before Tenant actions (update, cancel)
    Optional<Rental> findByIdAndTenantUserId(UUID id, UUID tenantUserId);

    // For security checks before Owner actions (approve, reject, view detail)
    Optional<Rental> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    // For GET /rentals/{rentalId} - Combined check (either tenant or owner)
    Optional<Rental> findByIdAndTenantUserIdOrOwnerUserId(UUID id, UUID tenantUserId, UUID ownerUserId); // Less common

    // For GET /rentals/internal/property/{propertyId}/status
    List<Rental> findByPropertyIdAndStatusIn(UUID propertyId, List<RentalStatus> statuses); // e.g., find ACTIVE rentals
    long countByPropertyIdAndStatus(UUID propertyId, RentalStatus status); // Example count query
}