package id.ac.ui.cs.advprog.papikos.rentals.service;

import id.ac.ui.cs.advprog.papikos.rentals.client.*;
import id.ac.ui.cs.advprog.papikos.rentals.config.RabbitMQConfig; // Import RabbitMQ Config
import id.ac.ui.cs.advprog.papikos.rentals.dto.*;
import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalEvent; // Import RentalEvent DTO
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.exception.*;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import id.ac.ui.cs.advprog.papikos.rentals.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import RabbitTemplate
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal; // For potential price in event
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // This will inject RabbitTemplate if it's final
public class RentalServiceImpl implements RentalService {

    private static final Logger log = LoggerFactory.getLogger(RentalServiceImpl.class);

    private final RentalRepository rentalRepository;
    private final KosServiceClient kosServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public RentalDto submitRentalApplication(UUID tenantUserId, RentalApplicationRequest request) {

        KosDetailsDto kosDetails = kosServiceClient.getKosDetails(request.getKosId());
        if (kosDetails == null) {
            throw new ResourceNotFoundException("Kos not found with ID: " + request.getKosId());
        }
        if (!kosDetails.isListed()) {
            throw new ValidationException("Kos with ID: " + request.getKosId() + " is not currently listed for rent.");
        }

        long activeRentals = kosServiceClient.getActiveRentalsCountForKos(request.getKosId());
        if (activeRentals >= kosDetails.getTotalRooms()) {
            throw new ValidationException("No rooms available for kos ID: " + request.getKosId());
        }

        Rental rental = new Rental();
        rental.setTenantUserId(tenantUserId);
        rental.setKosId(request.getKosId());
        rental.setOwnerUserId(kosDetails.getOwnerUserId());
        rental.setSubmittedTenantName(request.getSubmittedTenantName());
        rental.setSubmittedTenantPhone(request.getSubmittedTenantPhone());
        rental.setRentalStartDate(request.getRentalStartDate());
        rental.setRentalDurationMonths(request.getRentalDurationMonths());
        rental.setStatus(RentalStatus.PENDING_APPROVAL);

        Rental savedRental = rentalRepository.save(rental);
        log.info("Rental application submitted: {}", savedRental.getId());

        sendNotification(
                kosDetails.getOwnerUserId(),
                "RENTAL_APPLICATION_RECEIVED",
                "New Rental Application for " + kosDetails.getName(),
                request.getSubmittedTenantName() + " has applied to rent your kos '" + kosDetails.getName() + "'.",
                savedRental.getId()
        );

        RentalEvent event = new RentalEvent(
                savedRental.getId().toString(),
                savedRental.getTenantUserId().toString(),
                savedRental.getKosId().toString(),
                savedRental.getOwnerUserId().toString(),
                kosDetails.getName(),
                savedRental.getRentalStartDate(),
                savedRental.getRentalDurationMonths(),
                kosDetails.getPricePerMonth(),
                "RENTAL_APPLICATION_SUBMITTED",
                savedRental.getSubmittedTenantName(),
                savedRental.getSubmittedTenantPhone()
        );
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TOPIC_EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_RENTAL_CREATED,
                    event);
            log.info("Published rental.created event for rentalId {}: {}", savedRental.getId(), event);
        } catch (Exception e) {
            log.error("Failed to publish rental.created event for rentalId {}: {}", savedRental.getId(), e.getMessage(), e);
        }

        return mapToRentalDto(savedRental, kosDetails.getName());
    }

    @Override
    public List<RentalDto> getTenantRentals(UUID tenantUserId) {
        return rentalRepository.findByTenantUserId(tenantUserId).stream()
                .map(rental -> mapToRentalDto(rental, getKosName(rental.getKosId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<RentalDto> getOwnerRentals(UUID ownerUserId, RentalStatus status, UUID kosIdFilter) {
        List<Rental> rentals;
        if (kosIdFilter != null) {
            KosDetailsDto pDetails = kosServiceClient.getKosDetails(kosIdFilter);
            if(pDetails == null || !pDetails.getOwnerUserId().equals(ownerUserId)){
                throw new ForbiddenException("Access denied to kos rentals.");
            }
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
                .map(rental -> mapToRentalDto(rental, getKosName(rental.getKosId())))
                .collect(Collectors.toList());
    }


    @Override
    public RentalDto getRentalById(UUID rentalId, UUID userId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental not found with ID: " + rentalId));
        if (!Objects.equals(rental.getTenantUserId(), userId) && !Objects.equals(rental.getOwnerUserId(), userId)) {
            throw new ForbiddenException("User not authorized to view this rental.");
        }

        return mapToRentalDto(rental, getKosName(rental.getKosId()));
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
        log.info("Rental submission edited: {}", updatedRental.getId());
        return mapToRentalDto(updatedRental, getKosName(updatedRental.getKosId()));
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
        log.info("Rental {} cancelled by tenant {}", rentalId, userId);

        sendNotification(
                cancelledRental.getOwnerUserId(),
                "RENTAL_CANCELLED_BY_TENANT",
                "Rental Cancelled for " + getKosName(cancelledRental.getKosId()),
                "Rental application/agreement for kos '" + getKosName(cancelledRental.getKosId()) + "' (ID: " + cancelledRental.getId() + ") has been cancelled by the tenant.",
                cancelledRental.getId()
        );
        triggerVacancyCheck(cancelledRental.getKosId());
        return mapToRentalDto(cancelledRental, getKosName(cancelledRental.getKosId()));
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
        KosDetailsDto kosDetails = kosServiceClient.getKosDetails(rental.getKosId());
        long activeRentals = kosServiceClient.getActiveRentalsCountForKos(rental.getKosId());
        if (activeRentals >= kosDetails.getTotalRooms()) { // Check vacancy again before approval
            throw new ValidationException("No rooms available to approve this rental for kos ID: " + rental.getKosId());
        }

        rental.setStatus(RentalStatus.APPROVED);
        Rental approvedRental = rentalRepository.save(rental);
        log.info("Rental {} approved by owner {}", rentalId, ownerUserId);

        sendNotification(
                approvedRental.getTenantUserId(),
                "RENTAL_APPLICATION_APPROVED",
                "Your Rental Application is Approved!",
                "Congratulations! Your rental application for kos '" + getKosName(approvedRental.getKosId()) + "' has been approved. Please proceed with payment.",
                approvedRental.getId()
        );

        RentalEvent approvedEvent = new RentalEvent(
                approvedRental.getId().toString(),
                approvedRental.getTenantUserId().toString(),
                approvedRental.getKosId().toString(),
                approvedRental.getOwnerUserId().toString(),
                kosDetails.getName(),
                approvedRental.getRentalStartDate(),
                approvedRental.getRentalDurationMonths(),
                kosDetails.getPricePerMonth(), // Or a calculated final price
                RentalStatus.APPROVED.name(), // Explicitly "APPROVED"
                approvedRental.getSubmittedTenantName(),
                approvedRental.getSubmittedTenantPhone()
        );
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TOPIC_EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_RENTAL_APPROVED, // Use a specific key for approved
                    approvedEvent);
            log.info("Published rental.approved event for rentalId {}: {}", approvedRental.getId(), approvedEvent);
        } catch (Exception e) {
            log.error("Failed to publish rental.approved event for rentalId {}: {}", approvedRental.getId(), e.getMessage(), e);
        }

        return mapToRentalDto(approvedRental, getKosName(approvedRental.getKosId()));
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
        log.info("Rental {} rejected by owner {}", rentalId, ownerUserId);

        sendNotification(
                rejectedRental.getTenantUserId(),
                "RENTAL_APPLICATION_REJECTED",
                "Your Rental Application Status",
                "We regret to inform you that your rental application for kos '" + getKosName(rejectedRental.getKosId()) + "' has been rejected.",
                rejectedRental.getId()
        );
        return mapToRentalDto(rejectedRental, getKosName(rejectedRental.getKosId()));
    }

    private String getKosName(UUID kosId) {
        try {
            KosDetailsDto details = kosServiceClient.getKosDetails(kosId);
            return details != null ? details.getName() : "Unknown Kos";
        } catch (Exception e) {
            log.warn("Could not fetch kos name for ID {}: {}", kosId, e.getMessage());
            return "Kos " + kosId;
        }
    }

    private void sendNotification(UUID recipientUserId, String type, String title, String message, UUID relatedRentalId) {
        try {
            NotificationRequest notification = new NotificationRequest(recipientUserId, type, title, message, relatedRentalId);
            notificationServiceClient.sendNotification(notification);
            log.info("Notification of type {} sent to user {} for rental {}", type, recipientUserId, relatedRentalId);
        } catch (Exception e) {
            log.error("Failed to send notification type {} for rental {}: {}", type, relatedRentalId, e.getMessage());
        }
    }

    private void triggerVacancyCheck(UUID kosId) {
        log.info("Vacancy check triggered for kosId: {} due to rental cancellation/completion.", kosId);
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
