package id.ac.ui.cs.advprog.papikos.rentals.client;

import lombok.Getter;

import java.util.UUID;

@Getter
public class UserDetailsDto {
    private UUID userId;
    private String role; // e.g., "TENANT", "OWNER"

    public UserDetailsDto(UUID userId, String role) {
        this.userId = userId;
        this.role = role;
    }

}
