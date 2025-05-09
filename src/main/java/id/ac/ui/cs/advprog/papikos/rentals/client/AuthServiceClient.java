package id.ac.ui.cs.advprog.papikos.rentals.client;

import java.util.UUID;

public interface AuthServiceClient {
    UserDetailsDto getUserDetails(UUID userId);
}