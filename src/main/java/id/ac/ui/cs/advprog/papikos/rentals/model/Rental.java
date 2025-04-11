package id.ac.ui.cs.advprog.papikos.rentals.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Getter
public class Rental {
    private String id;
    private KosDummy kos;
    private TenantDummy tenant;
    private String fullName;
    private String phoneNumber;
    private String checkInDate;
    private int durationMonths;

    @Setter
    private String status;

    private static final List<String> VALID_STATUSES = Arrays.asList("PENDING", "CONFIRMED", "ONGOING", "COMPLETED", "CANCELLED");
    private static final String DEFAULT_STATUS = "PENDING";

    public Rental(String id, KosDummy kos, TenantDummy tenant, String fullName, String phoneNumber, String checkInDate, int durationMonths) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Rental ID cannot be null or empty.");
        }
        if (kos == null) {
            throw new IllegalArgumentException("Kos cannot be null.");
        }
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null.");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty.");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty.");
        }
        if (checkInDate == null || checkInDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Check-in date cannot be null or empty.");
        }
        if (durationMonths <= 0) {
            throw new IllegalArgumentException("Duration must be greater than 0 months.");
        }

        this.id = id;
        this.kos = kos;
        this.tenant = tenant;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.checkInDate = checkInDate;
        this.durationMonths = durationMonths;
        this.status = DEFAULT_STATUS;
    }

    public Rental(String id, KosDummy kos, TenantDummy tenant, String fullName, String phoneNumber, String checkInDate, int durationMonths, String status) {
        this(id, kos, tenant, fullName, phoneNumber, checkInDate, durationMonths);

        if (!isValidStatus(status)) {
            throw new IllegalArgumentException("Invalid status provided: " + status);
        }
        this.status = status;
    }

    private boolean isValidStatus(String status) {
        return status != null && VALID_STATUSES.contains(status.toUpperCase());
    }

    public void setStatus(String newStatus) {
        if (isValidStatus(newStatus)) {
            this.status = newStatus.toUpperCase();
        }
    }

}