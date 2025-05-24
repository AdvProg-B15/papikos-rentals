package id.ac.ui.cs.advprog.papikos.rentals.model;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RentalTest {

    private Rental rental;
    private UUID id;
    private UUID tenantUserId;
    private UUID kosId;
    private UUID ownerUserId;
    private String submittedTenantName;
    private String submittedTenantPhone;
    private LocalDate rentalStartDate;
    private Integer rentalDurationMonths;
    private LocalDate rentalEndDate;
    private RentalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        tenantUserId = UUID.randomUUID();
        kosId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        submittedTenantName = "Tenant Name";
        submittedTenantPhone = "08123456789";
        rentalStartDate = LocalDate.now().plusDays(1);
        rentalDurationMonths = 6;
        rentalEndDate = rentalStartDate.plusMonths(rentalDurationMonths);
        status = RentalStatus.PENDING_APPROVAL;
        createdAt = LocalDateTime.now().minusDays(1);
        updatedAt = LocalDateTime.now();

        rental = new Rental(id, tenantUserId, kosId, ownerUserId, submittedTenantName,
                submittedTenantPhone, rentalStartDate, rentalDurationMonths,
                rentalEndDate, status, createdAt, updatedAt);
    }

    @Test
    void testNoArgsConstructor() {
        Rental newRental = new Rental();
        assertNull(newRental.getId());
        assertEquals(RentalStatus.PENDING_APPROVAL, newRental.getStatus()); // Default status
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        assertEquals(id, rental.getId());
        assertEquals(tenantUserId, rental.getTenantUserId());
        assertEquals(kosId, rental.getKosId());
        assertEquals(ownerUserId, rental.getOwnerUserId());
        assertEquals(submittedTenantName, rental.getSubmittedTenantName());
        assertEquals(submittedTenantPhone, rental.getSubmittedTenantPhone());
        assertEquals(rentalStartDate, rental.getRentalStartDate());
        assertEquals(rentalDurationMonths, rental.getRentalDurationMonths());
        assertEquals(rentalEndDate, rental.getRentalEndDate());
        assertEquals(status, rental.getStatus());
        assertEquals(createdAt, rental.getCreatedAt());
        assertEquals(updatedAt, rental.getUpdatedAt());
    }

    @Test
    void testSetters() {
        Rental newRental = new Rental();
        UUID newId = UUID.randomUUID();
        UUID newTenantUserId = UUID.randomUUID();

        newRental.setId(newId);
        assertEquals(newId, newRental.getId());

        newRental.setTenantUserId(newTenantUserId);
        assertEquals(newTenantUserId, newRental.getTenantUserId());

        newRental.setKosId(kosId);
        assertEquals(kosId, newRental.getKosId());

        newRental.setOwnerUserId(ownerUserId);
        assertEquals(ownerUserId, newRental.getOwnerUserId());

        newRental.setSubmittedTenantName("New Name");
        assertEquals("New Name", newRental.getSubmittedTenantName());

        newRental.setSubmittedTenantPhone("9876543210");
        assertEquals("9876543210", newRental.getSubmittedTenantPhone());

        LocalDate newStartDate = LocalDate.now().plusDays(10);
        newRental.setRentalStartDate(newStartDate);
        assertEquals(newStartDate, newRental.getRentalStartDate());

        Integer newDuration = 12;
        newRental.setRentalDurationMonths(newDuration);
        assertEquals(newDuration, newRental.getRentalDurationMonths());

        LocalDate newEndDate = newStartDate.plusMonths(newDuration);
        newRental.setRentalEndDate(newEndDate);
        assertEquals(newEndDate, newRental.getRentalEndDate());

        newRental.setStatus(RentalStatus.APPROVED);
        assertEquals(RentalStatus.APPROVED, newRental.getStatus());

        LocalDateTime newCreatedAt = LocalDateTime.now().minusHours(5);
        newRental.setCreatedAt(newCreatedAt);
        assertEquals(newCreatedAt, newRental.getCreatedAt());

        LocalDateTime newUpdatedAt = LocalDateTime.now().minusHours(1);
        newRental.setUpdatedAt(newUpdatedAt);
        assertEquals(newUpdatedAt, newRental.getUpdatedAt());
    }

    @Test
    void testOnCreate_IdIsNull() {
        Rental newRental = new Rental();
        newRental.setRentalStartDate(LocalDate.now());
        newRental.setRentalDurationMonths(1);
        newRental.onCreate();

        assertNotNull(newRental.getId());
        assertNotNull(newRental.getCreatedAt());
        assertNotNull(newRental.getUpdatedAt());
        assertEquals(newRental.getCreatedAt(), newRental.getUpdatedAt());
        assertNotNull(newRental.getRentalEndDate());
        assertEquals(newRental.getRentalStartDate().plusMonths(1), newRental.getRentalEndDate());
    }

    @Test
    void testOnCreate_IdIsNotNull() {
        UUID preSetId = UUID.randomUUID();
        Rental newRental = new Rental();
        newRental.setId(preSetId);
        newRental.setRentalStartDate(LocalDate.now());
        newRental.setRentalDurationMonths(1);
        newRental.onCreate();

        assertEquals(preSetId, newRental.getId());
        assertNotNull(newRental.getCreatedAt());
        assertNotNull(newRental.getUpdatedAt());
    }

    @Test
    void testOnCreate_RentalEndDateCalculation() {
        Rental newRental = new Rental();
        LocalDate startDate = LocalDate.of(2024, 1, 15);
        int duration = 3;
        newRental.setRentalStartDate(startDate);
        newRental.setRentalDurationMonths(duration);
        newRental.onCreate();
        assertEquals(LocalDate.of(2024, 4, 15), newRental.getRentalEndDate());
    }

    @Test
    void testOnUpdate() throws InterruptedException {
        Rental newRental = new Rental();
        newRental.setRentalStartDate(LocalDate.now());
        newRental.setRentalDurationMonths(1);
        newRental.onCreate();

        LocalDateTime initialUpdatedAt = newRental.getUpdatedAt();
        Thread.sleep(10);

        newRental.setRentalDurationMonths(2);
        newRental.onUpdate();

        assertNotNull(newRental.getUpdatedAt());
        assertTrue(newRental.getUpdatedAt().isAfter(initialUpdatedAt));
        assertEquals(newRental.getRentalStartDate().plusMonths(2), newRental.getRentalEndDate());
    }

    @Test
    void testOnCreate_NullStartDateOrDuration() {
        Rental newRental = new Rental();
        newRental.onCreate();
        assertNull(newRental.getRentalEndDate());

        newRental.setRentalStartDate(LocalDate.now());
        newRental.onCreate();
        assertNull(newRental.getRentalEndDate());

        newRental.setRentalStartDate(null);
        newRental.setRentalDurationMonths(1);
        newRental.onCreate();
        assertNull(newRental.getRentalEndDate());
    }

    @Test
    void testOnUpdate_NullStartDateOrDuration() {
        Rental newRental = new Rental();
        newRental.onCreate();
        newRental.onUpdate();
        assertNull(newRental.getRentalEndDate());

        newRental.setRentalStartDate(LocalDate.now());
        newRental.onUpdate();
        assertNull(newRental.getRentalEndDate());

        newRental.setRentalStartDate(null);
        newRental.setRentalDurationMonths(1);
        newRental.onUpdate();
        assertNull(newRental.getRentalEndDate());
    }

    @Test
    void testDataAnnotation() {
        Rental rental1 = new Rental(id, tenantUserId, kosId, ownerUserId, submittedTenantName,
                submittedTenantPhone, rentalStartDate, rentalDurationMonths,
                rentalEndDate, status, createdAt, updatedAt);
        Rental rental2 = new Rental(id, tenantUserId, kosId, ownerUserId, submittedTenantName,
                submittedTenantPhone, rentalStartDate, rentalDurationMonths,
                rentalEndDate, status, createdAt, updatedAt);
        assertEquals(rental1, rental2);
        assertEquals(rental1.hashCode(), rental2.hashCode());
        assertTrue(rental1.toString().contains(id.toString()));
    }
}