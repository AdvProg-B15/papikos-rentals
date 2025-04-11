package id.ac.ui.cs.advprog.papikos.rentals.model;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RentalTest {

    // Dummy data untuk Kos dan Tenant
    KosDummy dummyKos;
    TenantDummy dummyTenant;
    String validFullName = "Alby Arto";
    String validPhoneNumber = "081234567890";
    String validCheckInDate = "2024-01-15";
    int validDuration = 6;
    String validId;

    @BeforeEach
    void setUp() {
        dummyKos = new KosDummy();
        dummyKos.setId(UUID.randomUUID().toString());
        dummyKos.setName("Kos Cascade");

        dummyTenant = new TenantDummy();
        dummyTenant.setId(UUID.randomUUID().toString());
        dummyTenant.setEmail("tenant@example.com");

        validId = UUID.randomUUID().toString();
    }

    @Test
    void testCreateRentalWithNullKos_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, null, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, validDuration);
        }, "Creating rental with null Kos should throw IllegalArgumentException");
    }

    @Test
    void testCreateRentalWithNullTenant_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, null, validFullName, validPhoneNumber, validCheckInDate, validDuration);
        }, "Creating rental with null Tenant should throw IllegalArgumentException");
    }

    @Test
    void testCreateRentalWithEmptyFullName_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, "", validPhoneNumber, validCheckInDate, validDuration);
        }, "Creating rental with empty Full Name should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, null, validPhoneNumber, validCheckInDate, validDuration);
        }, "Creating rental with null Full Name should throw IllegalArgumentException");
    }

    @Test
    void testCreateRentalWithEmptyPhoneNumber_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, validFullName, "", validCheckInDate, validDuration);
        }, "Creating rental with empty Phone Number should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, validFullName, null, validCheckInDate, validDuration);
        }, "Creating rental with null Phone Number should throw IllegalArgumentException");
    }

    @Test
    void testCreateRentalWithEmptyCheckInDate_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, "", validDuration);
        }, "Creating rental with empty Check-in Date should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, null, validDuration);
        }, "Creating rental with null Check-in Date should throw IllegalArgumentException");
    }

    @Test
    void testCreateRentalWithInvalidDuration_shouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, 0);
        }, "Creating rental with zero duration should throw IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, -1);
        }, "Creating rental with negative duration should throw IllegalArgumentException");
    }

    @Test
    void testCreateRentalWithDefaultStatus_shouldSetStatusToPending() {
        Rental rental = new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, validDuration);
        assertNotNull(rental);
        assertEquals(RentalStatus.PENDING, rental.getStatusEnum());
        assertEquals(validId, rental.getId());
        assertEquals(dummyKos, rental.getKos());
        assertEquals(dummyTenant, rental.getTenant());
        assertEquals(validFullName, rental.getFullName());
        assertEquals(validPhoneNumber, rental.getPhoneNumber());
        assertEquals(validCheckInDate, rental.getCheckInDate());
        assertEquals(validDuration, rental.getDurationMonths());
    }

    @Test
    void testCreateRentalWithExplicitValidStatus_shouldSetStatusCorrectly() {
        String statusStr = RentalStatus.CONFIRMED.name();
        Rental rental = new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, validDuration, statusStr);
        assertNotNull(rental);
        assertEquals(RentalStatus.CONFIRMED, rental.getStatusEnum());
    }

    @Test
    void testCreateRentalWithInvalidStatus_shouldThrowException() {
        String invalidStatus = "DI PROSES";
        assertThrows(IllegalArgumentException.class, () -> {
            new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, validDuration, invalidStatus);
        }, "Creating rental with invalid status should throw IllegalArgumentException");
    }

    @Test
    void testSetStatusWithValidStatus_shouldUpdateStatus() {
        Rental rental = new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, validDuration);
        assertEquals(RentalStatus.PENDING, rental.getStatusEnum());

        rental.setStatus(RentalStatus.ONGOING.name());
        assertEquals(RentalStatus.ONGOING, rental.getStatusEnum());

        rental.setStatus(RentalStatus.COMPLETED.name());
        assertEquals(RentalStatus.COMPLETED, rental.getStatusEnum());

        rental.setStatus(RentalStatus.CANCELLED.name());
        assertEquals(RentalStatus.CANCELLED, rental.getStatusEnum());
    }

    @Test
    void testSetStatusWithInvalidStatus_shouldNotUpdateStatus() {
        Rental rental = new Rental(validId, dummyKos, dummyTenant, validFullName, validPhoneNumber, validCheckInDate, validDuration, RentalStatus.CONFIRMED.name());
        RentalStatus initialStatus = rental.getStatusEnum();
        assertEquals(RentalStatus.CONFIRMED, initialStatus);

        String invalidStatus = "SELESAI";
        rental.setStatus(invalidStatus);

        assertEquals(initialStatus, rental.getStatusEnum());
    }
}