package id.ac.ui.cs.advprog.papikos.rentals.service;

import id.ac.ui.cs.advprog.papikos.rentals.dto.*;
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;

import java.util.List;

import java.util.UUID;

public interface RentalService {
    void tryFetchKosDetail(UUID kosId);

    RentalDto submitRentalApplication(UUID tenantUserId, RentalApplicationRequest request);
    List<RentalDto> getTenantRentals(UUID tenantUserId);
    List<RentalDto> getOwnerRentals(UUID ownerUserId, RentalStatus status, UUID propertyIdFilter);
    RentalDto getRentalById(UUID rentalId, UUID userId); // userId of the requester
    RentalDto editRentalSubmission(UUID rentalId, UUID tenantUserId, UpdateRentalSubmissionRequest request);
    RentalDto cancelRental(UUID rentalId, UUID userId, String userRole);
    RentalDto approveRental(UUID rentalId, UUID ownerUserId);
    RentalDto rejectRental(UUID rentalId, UUID ownerUserId);
}
