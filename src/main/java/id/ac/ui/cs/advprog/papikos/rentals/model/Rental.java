package id.ac.ui.cs.advprog.papikos.rentals.model;

import lombok.Getter;
import lombok.Setter;

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

    // Skeleton Konstruktor 1 (tanpa status)
    public Rental(String id, KosDummy kos, TenantDummy tenant, String fullName, String phoneNumber, String checkInDate, int durationMonths) {
        this.id = id;
        this.kos = kos;
        this.tenant = tenant;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.checkInDate = checkInDate;
        this.durationMonths = durationMonths;
        this.status = "PENDING";
    }

    // Skeleton Konstruktor 2 (dengan status)
    public Rental(String id, KosDummy kos, TenantDummy tenant, String fullName, String phoneNumber, String checkInDate, int durationMonths, String status) {
        this.id = id;
        this.kos = kos;
        this.tenant = tenant;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.checkInDate = checkInDate;
        this.durationMonths = durationMonths;
        this.status = status;
    }
}