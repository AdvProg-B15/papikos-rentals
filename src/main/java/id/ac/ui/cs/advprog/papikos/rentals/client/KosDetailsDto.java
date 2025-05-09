package id.ac.ui.cs.advprog.papikos.rentals.client;

import lombok.Getter;

import java.util.UUID;

@Getter
public class KosDetailsDto {
    private UUID kosId;
    private UUID ownerUserId;
    private String name;
    private int totalRooms;
    private boolean isListed;

    public KosDetailsDto(UUID kosId, UUID ownerUserId, String name, int totalRooms, boolean isListed) {
        this.kosId = kosId;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.totalRooms = totalRooms;
        this.isListed = isListed;
    }

    public boolean isListed() { return isListed; }
}
