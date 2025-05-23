package id.ac.ui.cs.advprog.papikos.rentals.controller;

import id.ac.ui.cs.advprog.papikos.rentals.dto.*;
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.response.ApiResponse;
import id.ac.ui.cs.advprog.papikos.rentals.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    /**
     * Helper method to extract UUID from Authentication principal name.
     * Throws IllegalArgumentException if parsing fails or principal is missing.
     */
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            log.warn("Authentication principal is required but missing.");
            throw new IllegalStateException("Authentication principal is required but missing.");
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException e) {
            log.error("Error parsing UUID from principal name: {}", authentication.getName(), e);
            throw new IllegalArgumentException("Invalid user identifier format in authentication token.");
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TENANT')")
    public ResponseEntity<ApiResponse<RentalDto>> submitRentalApplication(
            Authentication authentication, // Get user from Spring Security
            @Valid @RequestBody RentalApplicationRequest request) {
        UUID tenantUserId = getUserIdFromAuthentication(authentication);
        log.info("User {} submitting rental application", tenantUserId);
        RentalDto createdRental = rentalService.submitRentalApplication(tenantUserId, request);
        ApiResponse<RentalDto> response = ApiResponse.<RentalDto>builder()
                .status(HttpStatus.CREATED)
                .message("Rental application submitted successfully")
                .data(createdRental)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('TENANT')")
    public ResponseEntity<ApiResponse<List<RentalDto>>> getMyRentals(
            Authentication authentication) {
        UUID tenantUserId = getUserIdFromAuthentication(authentication);
        log.info("User {} fetching their rentals", tenantUserId);
        List<RentalDto> rentals = rentalService.getTenantRentals(tenantUserId);
        ApiResponse<List<RentalDto>> response = ApiResponse.<List<RentalDto>>builder()
                .status(HttpStatus.OK)
                .message("Tenant's rentals fetched successfully")
                .data(rentals)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/owner")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ApiResponse<List<RentalDto>>> getOwnerRentals(
            Authentication authentication,
            @RequestParam(required = false) RentalStatus status,
            @RequestParam(required = false) UUID propertyId) {
        UUID ownerUserId = getUserIdFromAuthentication(authentication);
        log.info("Owner {} fetching rentals. Status: {}, PropertyId: {}", ownerUserId, status, propertyId);
        List<RentalDto> rentals = rentalService.getOwnerRentals(ownerUserId, status, propertyId);
        ApiResponse<List<RentalDto>> response = ApiResponse.<List<RentalDto>>builder()
                .status(HttpStatus.OK)
                .message("Owner's rentals fetched successfully")
                .data(rentals)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{rentalId}")
    @PreAuthorize("hasAnyAuthority('TENANT', 'OWNER')")
    public ResponseEntity<ApiResponse<RentalDto>> getRentalById(
            @PathVariable UUID rentalId,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        log.info("User {} fetching rental by ID: {}", userId, rentalId);
        RentalDto rental = rentalService.getRentalById(rentalId, userId); // Service handles if this user can see this rental
        ApiResponse<RentalDto> response = ApiResponse.<RentalDto>builder()
                .status(HttpStatus.OK)
                .message("Rental details fetched successfully")
                .data(rental)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rentalId}")
    @PreAuthorize("hasAuthority('TENANT')") // Only the tenant who submitted can edit
    public ResponseEntity<ApiResponse<RentalDto>> editRentalSubmission(
            @PathVariable UUID rentalId,
            Authentication authentication,
            @Valid @RequestBody UpdateRentalSubmissionRequest request) {
        UUID tenantUserId = getUserIdFromAuthentication(authentication);
        log.info("Tenant {} editing rental submission: {}", tenantUserId, rentalId);
        RentalDto updatedRental = rentalService.editRentalSubmission(rentalId, tenantUserId, request);
        ApiResponse<RentalDto> response = ApiResponse.<RentalDto>builder()
                .status(HttpStatus.OK)
                .message("Rental submission updated successfully")
                .data(updatedRental)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rentalId}/cancel")
    @PreAuthorize("hasAnyAuthority('TENANT', 'OWNER')")
    public ResponseEntity<ApiResponse<RentalDto>> cancelRental(
            @PathVariable UUID rentalId,
            Authentication authentication) {
        UUID userId = getUserIdFromAuthentication(authentication);
        String userRole = authentication.getAuthorities().iterator().next().getAuthority();
        log.info("User {} cancelling rental: {}", userId, rentalId);
        RentalDto cancelledRental = rentalService.cancelRental(rentalId, userId, userRole);
        ApiResponse<RentalDto> response = ApiResponse.<RentalDto>builder()
                .status(HttpStatus.OK)
                .message("Rental cancelled successfully")
                .data(cancelledRental)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rentalId}/approve")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ApiResponse<RentalDto>> approveRental(
            @PathVariable UUID rentalId,
            Authentication authentication) {
        UUID ownerUserId = getUserIdFromAuthentication(authentication);
        log.info("Owner {} approving rental: {}", ownerUserId, rentalId);
        RentalDto approvedRental = rentalService.approveRental(rentalId, ownerUserId);
        ApiResponse<RentalDto> response = ApiResponse.<RentalDto>builder()
                .status(HttpStatus.OK)
                .message("Rental approved successfully")
                .data(approvedRental)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{rentalId}/reject")
    @PreAuthorize("hasAuthority('OWNER')")
    public ResponseEntity<ApiResponse<RentalDto>> rejectRental(
            @PathVariable UUID rentalId,
            Authentication authentication) {
        UUID ownerUserId = getUserIdFromAuthentication(authentication);
        log.info("Owner {} rejecting rental: {}", ownerUserId, rentalId);
        RentalDto rejectedRental = rentalService.rejectRental(rentalId, ownerUserId);
        ApiResponse<RentalDto> response = ApiResponse.<RentalDto>builder()
                .status(HttpStatus.OK)
                .message("Rental rejected successfully")
                .data(rejectedRental)
                .build();
        return ResponseEntity.ok(response);
    }
}