package id.ac.ui.cs.advprog.papikos.rentals.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.papikos.rentals.client.*;
import id.ac.ui.cs.advprog.papikos.rentals.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.papikos.rentals.dto.*;
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.exception.*;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import id.ac.ui.cs.advprog.papikos.rentals.repository.RentalRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private static final Logger log = LoggerFactory.getLogger(RentalServiceImpl.class);

    private final RentalRepository rentalRepository;
    private final KosServiceClient kosServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final RabbitTemplate rabbitTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public void tryFetchKosDetail(UUID kosId) {
        try {
            KosDetailsDto kos=  fetchKosDetails(kosId);
            log.info("Fetched Kos details successfully: {} {}", kos.getName(), kos.getId());
        } catch (Exception e) {
            log.error("Error fetching Kos details: {}", e.getMessage(), e);
            throw new ServiceUnavailableException("Failed to fetch Kos details. Please try again later.");
        }
    }

    // Helper method to fetch and unwrap KosDetailsDto
    KosDetailsDto fetchKosDetails(UUID kosId) {
        KosApiResponseWrapper<KosDetailsDto> responseWrapper;
        try {
            responseWrapper = kosServiceClient.getKosDetailsApiResponse(kosId);

        } catch (FeignException e) {
            log.error("FeignException while fetching Kos details for ID {}: Status {}, Body {}",
                    kosId, e.status(), e.contentUTF8(), e);
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException("Kos not found with ID: " + kosId + " (via Kos Service).");
            }
            throw new ServiceUnavailableException("Error fetching Kos details: Kos service unavailable or unexpected error. " + e.getMessage());
        }

        if (responseWrapper == null) {
            throw new ServiceUnavailableException("No response from Kos service for Kos ID: " + kosId);
        }

        // Check the status from the API response wrapper itself
        if (responseWrapper.getStatus() != HttpStatus.OK.value()) {
            log.warn("Kos service returned non-OK status ({}) for Kos ID {}: {}",
                    responseWrapper.getStatus(), kosId, responseWrapper.getMessage());
            if (responseWrapper.getStatus() == HttpStatus.NOT_FOUND.value()) {
                throw new ResourceNotFoundException("Kos not found with ID: " + kosId + " (Reported by Kos Service: " + responseWrapper.getMessage() + ")");
            }
            // Handle other non-OK statuses as appropriate
            throw new ServiceInteractionException("Kos service reported an issue for Kos ID " + kosId + ": " +
                    responseWrapper.getStatus() + " - " + responseWrapper.getMessage());
        }

        if (responseWrapper.getData() == null) {
            log.error("Kos service returned OK status but no data for Kos ID {}", kosId);
            throw new ResourceNotFoundException("Kos details data not found for ID: " + kosId + " (Kos Service returned no data).");
        }
        return responseWrapper.getData();
    }


    @Override
    @Transactional
    public RentalDto submitRentalApplication(UUID tenantUserId, RentalApplicationRequest request) {
        KosDetailsDto kosDetails = fetchKosDetails(request.getKosId()); // Use helper

        if (!kosDetails.isListed()) {
            throw new ValidationException("Kos with ID: " + request.getKosId() + " is not currently listed for rent.");
        }

        List<RentalStatus> consideredActiveStatuses = Arrays.asList(RentalStatus.APPROVED, RentalStatus.ACTIVE);
        List<Rental> activeRentalsForThisKos = rentalRepository.findByKosIdAndStatusIn(request.getKosId(), consideredActiveStatuses);

        if (activeRentalsForThisKos.size() >= kosDetails.getNumRooms()) {
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
                "New Rental Application for " + kosDetails.getName(),
                request.getSubmittedTenantName() + " has applied to rent your kos '" + kosDetails.getName() + "'.",
                savedRental.getId(),
                savedRental.getKosId()
        );

        RentalEvent event = RentalEvent.builder()
                .rentalId(savedRental.getId().toString())
                .userId(savedRental.getTenantUserId().toString())
                .kosId(savedRental.getKosId().toString())
                .kosOwnerId(savedRental.getOwnerUserId().toString())
                .kosName(kosDetails.getName())
                .bookingDate(savedRental.getRentalStartDate())
                .rentalDurationMonths(savedRental.getRentalDurationMonths())
                .price(kosDetails.getMonthlyRentPrice())
                .status("RENTAL_APPLICATION_SUBMITTED")
                .tenantName(savedRental.getSubmittedTenantName())
                .tenantPhone(savedRental.getSubmittedTenantPhone())
                .timestamp(LocalDateTime.now().toString())
                .build();

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
            KosDetailsDto pDetails = fetchKosDetails(kosIdFilter); // Use helper
            if (!pDetails.getOwnerUserId().equals(ownerUserId)) {
                log.warn("Owner {} attempted to access rentals for kos {} they do not own.", ownerUserId, kosIdFilter);
                throw new ForbiddenException("Access denied to kos rentals. You do not own this kos.");
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
        if (request.getSubmittedTenantPhone() != null)
            rental.setSubmittedTenantPhone(request.getSubmittedTenantPhone());
        if (request.getRentalStartDate() != null) rental.setRentalStartDate(request.getRentalStartDate());
        if (request.getRentalDurationMonths() != null)
            rental.setRentalDurationMonths(request.getRentalDurationMonths());

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
            throw new ForbiddenException("User not authorized to cancel this rental through this flow.");
        }
        if (!(rental.getStatus() == RentalStatus.PENDING_APPROVAL || rental.getStatus() == RentalStatus.APPROVED)) {
            throw new ValidationException("Rental cannot be cancelled by tenant in its current state: " + rental.getStatus() + ". Contact owner.");
        }

        rental.setStatus(RentalStatus.CANCELLED);
        Rental cancelledRental = rentalRepository.save(rental);
        log.info("Rental {} cancelled by user {} (role: {})", rentalId, userId, userRole);

        sendNotification(
                cancelledRental.getOwnerUserId(),
                "Rental Cancelled for " + getKosName(cancelledRental.getKosId()),
                "Rental application/agreement for kos '" + getKosName(cancelledRental.getKosId()) + "' (ID: " + cancelledRental.getId() + ") has been cancelled by the tenant.",
                cancelledRental.getId(),
                cancelledRental.getKosId()
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

        KosDetailsDto kosDetails = fetchKosDetails(rental.getKosId()); // Use helper

        List<RentalStatus> consideredActiveStatuses = Arrays.asList(RentalStatus.APPROVED, RentalStatus.ACTIVE);
        List<Rental> activeRentalsForThisKos = rentalRepository.findByKosIdAndStatusIn(rental.getKosId(), consideredActiveStatuses);

        if (activeRentalsForThisKos.size() >= kosDetails.getNumRooms()) {
            throw new ValidationException("No rooms available to approve this rental for kos ID: " + rental.getKosId() + ". Another rental might have been approved concurrently.");
        }

        rental.setStatus(RentalStatus.APPROVED);
        Rental approvedRental = rentalRepository.save(rental);
        log.info("Rental {} approved by owner {}", rentalId, ownerUserId);

        sendNotification(
                approvedRental.getTenantUserId(),
                "Your Rental Application is Approved!",
                "Congratulations! Your rental application for kos '" + getKosName(approvedRental.getKosId()) + "' has been approved. Please proceed with payment if applicable.",
                approvedRental.getId(),
                approvedRental.getKosId()
        );

        id.ac.ui.cs.advprog.papikos.rentals.dto.RentalEvent approvedEvent = id.ac.ui.cs.advprog.papikos.rentals.dto.RentalEvent.builder()
                .rentalId(approvedRental.getId().toString())
                .userId(approvedRental.getTenantUserId().toString())
                .kosId(approvedRental.getKosId().toString())
                .kosOwnerId(approvedRental.getOwnerUserId().toString())
                .kosName(kosDetails.getName())
                .bookingDate(approvedRental.getRentalStartDate())
                .rentalDurationMonths(approvedRental.getRentalDurationMonths())
                .price(kosDetails.getMonthlyRentPrice())
                .status(RentalStatus.APPROVED.name())
                .tenantName(approvedRental.getSubmittedTenantName())
                .tenantPhone(approvedRental.getSubmittedTenantPhone())
                .timestamp(LocalDateTime.now().toString())
                .build();
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TOPIC_EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_RENTAL_APPROVED,
                    approvedEvent);
            log.info("Published rental.approved event for rentalId {}: {}", approvedRental.getId(), approvedEvent);
        } catch (Exception e) {
            log.error("Failed to publish rental.approved event for rentalId {}: {}", approvedRental.getId(), e.getMessage(), e);
        }

        return mapToRentalDto(approvedRental, kosDetails.getName());
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
                "Your Rental Application Status",
                "We regret to inform you that your rental application for kos '" + getKosName(rejectedRental.getKosId()) + "' has been rejected.",
                rejectedRental.getId(),
                rejectedRental.getKosId()
        );
        triggerVacancyCheck(rejectedRental.getKosId());
        return mapToRentalDto(rejectedRental, getKosName(rejectedRental.getKosId()));
    }

    String getKosName(UUID kosId) {
        try {
            KosDetailsDto details = fetchKosDetails(kosId);
            return details.getName();
        } catch (ResourceNotFoundException | ServiceUnavailableException | ServiceInteractionException e) {
            log.warn("Could not retrieve Kos name for ID {} due to: {}. Using placeholder.", kosId, e.getMessage());
            return "Kos (ID: " + kosId.toString().substring(0, 8) + ")";
        }
    }

    void sendNotification(UUID recipientUserId, String title, String message, UUID relatedRentalId, UUID relatedKosId) {
        try {
            NotificationRequest notificationPayload = new NotificationRequest(
                    relatedRentalId,
                    recipientUserId,
                    relatedKosId,
                    title,
                    message
            );
            ResponseEntity<Void> response = notificationServiceClient.sendNotification(notificationPayload);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Rental update notification sent successfully for rental {}, recipient {}", relatedRentalId, recipientUserId);
            } else {
                log.warn("Failed to send rental update notification for rental {}, recipient {}. Status: {}",
                        relatedRentalId, recipientUserId, response.getStatusCode());
            }
        } catch (FeignException e) {
            log.error("FeignException while sending rental update notification for rental {} to {}: Status {}, Body {}. Error: {}",
                    relatedRentalId, recipientUserId, e.status(), e.contentUTF8(), e.getMessage(), e);
            throw e; // Melempar kembali FeignException (unchecked) akan menyebabkan rollback jika tidak ditangani lebih lanjut.
        } catch (Exception e) {
            log.error("Generic error sending rental update notification for rental {} to {}: {}",
                    relatedRentalId, recipientUserId, e.getMessage(), e);
            throw new ServiceInteractionException("Failed to send notification due to an unexpected error."); // Bungkus sebagai runtime
        }
    }

    void triggerVacancyCheck(UUID kosId) {
        log.info("Vacancy check potentially triggered for kosId: {} due to rental status change (cancelled/rejected).", kosId);
    }

    RentalDto mapToRentalDto(Rental rental, String kosName) {
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

    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
    static class ServiceInteractionException extends RuntimeException {
        public ServiceInteractionException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}