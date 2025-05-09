package id.ac.ui.cs.advprog.papikos.rentals.repository;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID> {
    List<Rental> findByTenantUserId(UUID tenantUserId);
    List<Rental> findByOwnerUserId(UUID ownerUserId);
    List<Rental> findByKosIdAndStatusIn(UUID kosId, List<RentalStatus> statuses);
}