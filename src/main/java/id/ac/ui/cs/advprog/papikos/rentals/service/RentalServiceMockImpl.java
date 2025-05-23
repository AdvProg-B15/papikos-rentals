package id.ac.ui.cs.advprog.papikos.rentals.service;

import id.ac.ui.cs.advprog.papikos.rentals.client.*;
import id.ac.ui.cs.advprog.papikos.rentals.dto.*;
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.exception.*;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import id.ac.ui.cs.advprog.papikos.rentals.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("test") // This profile will be used for testing
@RequiredArgsConstructor
public class RentalServiceMockImpl implements RentalService {

    private static final Logger log = LoggerFactory.getLogger(RentalServiceMockImpl.class);

    private final RentalRepository rentalRepository;

    // Fake KosService implementation
    private KosDetailsDto getMockKosDetails(UUID kosId) {
        // Return a mock KosDetailsDto
        return new KosDetailsDto(
                kosId,
                UUID.fromString("00000000-0000-0000-0000-000000000001"), // Mock owner ID
                "Mock Kos Name",
                10, // Total rooms
                true // Is listed
        );
    }

    // Fake AuthService implementation
    private UserDetailsDto getMockUserDetails(UUID userId) {
        // Just returning a role based on the UUID passed
        String role = userId.toString().endsWith("1") ? "OWNER" : "TENANT";
        return new UserDetailsDto(userId, role);
    }

    // Fake notification sending
    private ResponseEntity<Void> sendMockNotification(NotificationRequest request) {
        log.info("Mock notification sent: {} to {}", request.getType(), request.getRecipientUserId());
        return ResponseEntity.ok().build();
    }

    @Override
    @Transactional
    public RentalDto submitRentalApplication(UUID tenantUserId, RentalApplicationRequest request) {
        // Mock KosService call
        KosDetailsDto kosDetails = getMockKosDetails(request.getKosId());

        Rental rental = new Rental();
        rental.setTenantUserId(tenantUserId);
        rental.setKosId(request.getKosId());
        rental.setOwnerUserId(kosDetails.getOwnerUserId());
        rental.setSubmittedTenantName(request.getSubmittedTenantName());
        rental.setSubmittedTenantPhone(request.getSubmittedTenantPhone());
        rental.setRentalStartDate(request.getRentalStartDate());
        rental.setRentalDurationMonths(request.getRentalDurationMonths());
        rental.setStatus(RentalStatus.PENDING_APPROVAL);

        // Calculate end date
        rental.setRentalEndDate(request.getRentalStartDate().plusMonths(request.getRentalDurationMonths()));

        Rental savedRental = rentalRepository.save(rental);
        log.info("Mock rental application submitted: {}", savedRental.getId());

        // Mock notification
        log.info("Mock notification for new rental application would be sent to owner: {}", kosDetails.getOwnerUserId());

        return mapToRentalDto(savedRental, kosDetails.getName());
    }

    @Override
    public List<RentalDto> getTenantRentals(UUID tenantUserId) {
        return rentalRepository.findByTenantUserId(tenantUserId).stream()
                .map(rental -> mapToRentalDto(rental, "Mock Kos " + rental.getKosId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<RentalDto> getOwnerRentals(UUID ownerUserId, RentalStatus status, UUID kosIdFilter) {
        List<Rental> rentals;
        if (kosIdFilter != null) {
            rentals = rentalRepository.findAll().stream()
                    .filter(r -> r.getOwnerUserId().equals(ownerUserId) && r.getKosId().equals(kosIdFilter))
                    .filter(r -> status == null || r.getStatus() == status)
                    .collect(Collectors.toList());
        } else {
            rentals = rentalRepository.findByOwnerUserId(ownerUserId).stream()
                    .filter(r -> status == null || r.getStatus() == status)
                    .collect(Collectors.toList());
        }

        return rentals.stream()
                .map(rental -> mapToRentalDto(rental, "Mock Kos " + rental.getKosId()))
                .collect(Collectors.toList());
    }

    @Override
    public RentalDto getRentalById(UUID rentalId, UUID userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + rentalId));

        if (!Objects.equals(rental.getTenantUserId(), userId) && !Objects.equals(rental.getOwnerUserId(), userId)) {
            throw new ForbiddenException("User not authorized to view this rental.");
        }

        return mapToRentalDto(rental, "Mock Kos " + rental.getKosId());
    }

    @Override
    @Transactional
    public RentalDto editRentalSubmission(UUID rentalId, UUID tenantUserId, UpdateRentalSubmissionRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + rentalId));

        if (!Objects.equals(rental.getTenantUserId(), tenantUserId)) {
            throw new ForbiddenException("User not authorized to edit this rental submission.");
        }
        if (rental.getStatus() != RentalStatus.PENDING_APPROVAL) {
            throw new ValidationException("Cannot edit rental submission that is not in PENDING_APPROVAL status.");
        }

        if (request.getSubmittedTenantName() != null) rental.setSubmittedTenantName(request.getSubmittedTenantName());
        if (request.getSubmittedTenantPhone() != null) rental.setSubmittedTenantPhone(request.getSubmittedTenantPhone());
        if (request.getRentalStartDate() != null) rental.setRentalStartDate(request.getRentalStartDate());
        if (request.getRentalDurationMonths() != null) rental.setRentalDurationMonths(request.getRentalDurationMonths());

        Rental updatedRental = rentalRepository.save(rental);
        log.info("Mock rental submission edited: {}", updatedRental.getId());
        return mapToRentalDto(updatedRental, "Mock Kos " + rental.getKosId());
    }

    @Override
    @Transactional
    public RentalDto cancelRental(UUID rentalId, UUID userId, String userRole) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + rentalId));

        boolean isTenant = "TENANT".equals(userRole) && Objects.equals(rental.getTenantUserId(), userId);

        if (!isTenant) {
            throw new ForbiddenException("User not authorized to cancel this rental.");
        }
        if (rental.getStatus() == RentalStatus.COMPLETED || rental.getStatus() == RentalStatus.CANCELLED) {
            throw new ValidationException("Rental cannot be cancelled in its current state: " + rental.getStatus());
        }

        rental.setStatus(RentalStatus.CANCELLED);
        Rental cancelledRental = rentalRepository.save(rental);
        log.info("Mock rental {} cancelled by tenant {}", rentalId, userId);

        // Mock notification
        log.info("Mock notification for rental cancellation would be sent to owner: {}", rental.getOwnerUserId());

        return mapToRentalDto(cancelledRental, "Mock Kos " + rental.getKosId());
    }

    @Override
    @Transactional
    public RentalDto approveRental(UUID rentalId, UUID ownerUserId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + rentalId));

        if (!Objects.equals(rental.getOwnerUserId(), ownerUserId)) {
            throw new ForbiddenException("User not authorized to approve this rental.");
        }
        if (rental.getStatus() != RentalStatus.PENDING_APPROVAL) {
            throw new ValidationException("Rental can only be approved if status is PENDING_APPROVAL.");
        }

        rental.setStatus(RentalStatus.APPROVED);
        Rental approvedRental = rentalRepository.save(rental);
        log.info("Mock rental {} approved by owner {}", rentalId, ownerUserId);

        // Mock notification
        log.info("Mock notification for rental approval would be sent to tenant: {}", rental.getTenantUserId());

        return mapToRentalDto(approvedRental, "Mock Kos " + rental.getKosId());
    }

    @Override
    @Transactional
    public RentalDto rejectRental(UUID rentalId, UUID ownerUserId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + rentalId));

        if (!Objects.equals(rental.getOwnerUserId(), ownerUserId)) {
            throw new ForbiddenException("User not authorized to reject this rental.");
        }
        if (rental.getStatus() != RentalStatus.PENDING_APPROVAL) {
            throw new ValidationException("Rental can only be rejected if status is PENDING_APPROVAL.");
        }

        rental.setStatus(RentalStatus.REJECTED);
        Rental rejectedRental = rentalRepository.save(rental);
        log.info("Mock rental {} rejected by owner {}", rentalId, ownerUserId);

        // Mock notification
        log.info("Mock notification for rental rejection would be sent to tenant: {}", rental.getTenantUserId());

        return mapToRentalDto(rejectedRental, "Mock Kos " + rental.getKosId());
    }

    private RentalDto mapToRentalDto(Rental rental, String kosName) {
        return RentalDto.builder()
                .rentalId(rental.getId())
                .tenantUserId(rental.getTenantUserId())
                .kosId(rental.getKosId())
                .ownerUserId(rental.getOwnerUserId())
                .kosName(kosName)
                .submittedTenantName(rental.getSubmittedTenantName())
                .submittedTenantPhone(rental.getSubmittedTenantPhone())
                .rentalStartDate(rental.getRentalStartDate())
                .rentalDurationMonths(rental.getRentalDurationMonths())
                .rentalEndDate(rental.getRentalEndDate())
                .status(rental.getStatus().toString())
                .createdAt(rental.getCreatedAt())
                .updatedAt(rental.getUpdatedAt())
                .build();
    }
}