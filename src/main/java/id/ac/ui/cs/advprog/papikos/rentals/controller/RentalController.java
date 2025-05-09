package id.ac.ui.cs.advprog.papikos.rentals.controller;

import id.ac.ui.cs.advprog.papikos.rentals.dto.*;
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.response.ApiResponse;
import id.ac.ui.cs.advprog.papikos.rentals.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @PostMapping
    public ResponseEntity<ApiResponse<RentalDto>> submitRentalApplication(
            @RequestHeader(value = "X-User-ID") UUID tenantUserId,
            @RequestHeader(value = "X-User-Role") String userRole, // For role-based logic if needed directly here
            @Valid @RequestBody RentalApplicationRequest request) {
        // Add role check if necessary, e.g. if (!"TENANT".equals(userRole)) throw new Forbidden...
        RentalDto createdRental = rentalService.submitRentalApplication(tenantUserId, request);
        return new ResponseEntity<>(ApiResponse.<RentalDto>builder().created(createdRental), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<RentalDto>>> getMyRentals(
            @RequestHeader(value = "X-User-ID") UUID tenantUserId,
            @RequestHeader(value = "X-User-Role") String userRole) {
        // Add role check if necessary
        List<RentalDto> rentals = rentalService.getTenantRentals(tenantUserId);
        return ResponseEntity.ok(ApiResponse.<List<RentalDto>>builder().ok(rentals));
    }

    @GetMapping("/owner")
    public ResponseEntity<ApiResponse<List<RentalDto>>> getOwnerRentals(
            @RequestHeader(value = "X-User-ID") UUID ownerUserId,
            @RequestHeader(value = "X-User-Role") String userRole,
            @RequestParam(required = false) RentalStatus status,
            @RequestParam(required = false) UUID propertyId) {
        // Add role check if necessary
        List<RentalDto> rentals = rentalService.getOwnerRentals(ownerUserId, status, propertyId);
        return ResponseEntity.ok(ApiResponse.<List<RentalDto>>builder().ok(rentals));
    }

    @GetMapping("/{rentalId}")
    public ResponseEntity<ApiResponse<RentalDto>> getRentalById(
            @PathVariable UUID rentalId,
            @RequestHeader(value = "X-User-ID") UUID userId,
            @RequestHeader(value = "X-User-Role") String userRole) {
        RentalDto rental = rentalService.getRentalById(rentalId, userId); // Service handles auth logic
        return ResponseEntity.ok(ApiResponse.<RentalDto>builder().ok(rental));
    }

    @PatchMapping("/{rentalId}")
    public ResponseEntity<ApiResponse<RentalDto>> editRentalSubmission(
            @PathVariable UUID rentalId,
            @RequestHeader(value = "X-User-ID") UUID tenantUserId,
            @RequestHeader(value = "X-User-Role") String userRole,
            @Valid @RequestBody UpdateRentalSubmissionRequest request) {
        // Add role check if necessary
        RentalDto updatedRental = rentalService.editRentalSubmission(rentalId, tenantUserId, request);
        return ResponseEntity.ok(ApiResponse.<RentalDto>builder().ok(updatedRental));
    }

    @PatchMapping("/{rentalId}/cancel")
    public ResponseEntity<ApiResponse<RentalDto>> cancelRental(
            @PathVariable UUID rentalId,
            @RequestHeader(value = "X-User-ID") UUID userId,
            @RequestHeader(value = "X-User-Role") String userRole) {
        RentalDto cancelledRental = rentalService.cancelRental(rentalId, userId, userRole);
        return ResponseEntity.ok(ApiResponse.<RentalDto>builder().ok(cancelledRental));
    }

    @PatchMapping("/{rentalId}/approve")
    public ResponseEntity<ApiResponse<RentalDto>> approveRental(
            @PathVariable UUID rentalId,
            @RequestHeader(value = "X-User-ID") UUID ownerUserId,
            @RequestHeader(value = "X-User-Role") String userRole) {
        // Add role check if necessary
        RentalDto approvedRental = rentalService.approveRental(rentalId, ownerUserId);
        return ResponseEntity.ok(ApiResponse.<RentalDto>builder().ok(approvedRental));
    }

    @PatchMapping("/{rentalId}/reject")
    public ResponseEntity<ApiResponse<RentalDto>> rejectRental(
            @PathVariable UUID rentalId,
            @RequestHeader(value = "X-User-ID") UUID ownerUserId,
            @RequestHeader(value = "X-User-Role") String userRole) {
        // Add role check if necessary
        RentalDto rejectedRental = rentalService.rejectRental(rentalId, ownerUserId);
        return ResponseEntity.ok(ApiResponse.<RentalDto>builder().ok(rejectedRental));
    }
}