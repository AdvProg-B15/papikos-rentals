package id.ac.ui.cs.advprog.papikos.rentals.enums;

import java.util.Arrays;
import java.util.Optional;

public enum RentalStatus {
    PENDING_APPROVAL, // Default
    APPROVED,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    REJECTED;

    public static boolean contains(String param) {
        if (param == null) return false;
        return Arrays.stream(values()).anyMatch(s -> s.name().equalsIgnoreCase(param));
    }
    public static Optional<RentalStatus> fromString(String param) {
        if (param == null) return Optional.empty();
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(param))
                .findFirst();
    }
}