package id.ac.ui.cs.advprog.papikos.rentals.client;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class KosDetailsDto {
    private UUID kosId;
    private UUID ownerUserId;
    private String name;
    private int totalRooms;
    private boolean isListed;
    private BigDecimal pricePerMonth;

    public KosDetailsDto(UUID kosId, UUID ownerUserId, String name, int totalRooms, boolean isListed, BigDecimal pricePerMonth) {
        this.kosId = kosId;
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.totalRooms = totalRooms;
        this.isListed = isListed;
        this.pricePerMonth = pricePerMonth;
    }

    public boolean isListed() { return isListed; }
}
