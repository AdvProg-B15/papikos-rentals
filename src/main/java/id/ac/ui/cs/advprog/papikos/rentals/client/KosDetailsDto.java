package id.ac.ui.cs.advprog.papikos.rentals.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KosDetailsDto {

    @JsonProperty("id")
    private UUID kosId;

    private UUID ownerUserId;

    private String name;

    @JsonProperty("numRooms")
    private int totalRooms;

    @JsonProperty("isListed")
    private boolean listed;

    private BigDecimal monthlyRentPrice;

    public boolean isListed() {
        return listed;
    }
    public void setListed(boolean listed) { this.listed = listed; }
}