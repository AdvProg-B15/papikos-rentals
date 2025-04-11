package id.ac.ui.cs.advprog.papikos.rentals.enums;

import java.util.Arrays;
import java.util.Optional;

public enum RentalStatus {
    PENDING,
    CONFIRMED,
    ONGOING,
    COMPLETED,
    CANCELLED;

    public static boolean contains(String param) {
        if (param == null) {
            return false;
        }
        return Arrays.stream(RentalStatus.values())
                .anyMatch(status -> status.name().equalsIgnoreCase(param));
    }

    public static Optional<RentalStatus> fromString(String param) {
        if (param == null) {
            return Optional.empty();
        }
        return Arrays.stream(RentalStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(param))
                .findFirst();
    }
}