package id.ac.ui.cs.advprog.papikos.rentals.client;

import java.util.UUID;

public interface KosServiceClient {
    KosDetailsDto getKosDetails(UUID kosId);
    long getActiveRentalsCountForKos(UUID kosId);
}