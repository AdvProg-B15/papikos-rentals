package id.ac.ui.cs.advprog.papikos.rentals.service.async;

import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import java.util.concurrent.CompletableFuture;

public interface AsyncRentalProcessingService {
    CompletableFuture<Void> performPostApprovalTasks(Rental approvedRental);
    CompletableFuture<Void> performPostCancellationTasks(Rental cancelledRental);
}