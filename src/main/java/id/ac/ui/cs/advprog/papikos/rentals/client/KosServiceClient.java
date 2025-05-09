package id.ac.ui.cs.advprog.papikos.rentals.client;

import java.util.UUID;

public interface KosServiceClient {
    KosDetailsDto getkosDetails(UUID kosId);
    UUID getActiveRentalsCountForKos(UUID kosId);
}