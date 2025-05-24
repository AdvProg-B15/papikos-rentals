package id.ac.ui.cs.advprog.papikos.rentals.enums;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class RentalStatusTest {

    @Test
    void testContains_ValidStatus() {
        assertTrue(RentalStatus.contains("PENDING_APPROVAL"));
        assertTrue(RentalStatus.contains("pending_approval"));
        assertTrue(RentalStatus.contains("APPROVED"));
        assertTrue(RentalStatus.contains("approved"));
        assertTrue(RentalStatus.contains("ACTIVE"));
        assertTrue(RentalStatus.contains("COMPLETED"));
        assertTrue(RentalStatus.contains("CANCELLED"));
        assertTrue(RentalStatus.contains("REJECTED"));
    }

    @Test
    void testContains_InvalidStatus() {
        assertFalse(RentalStatus.contains("UNKNOWN_STATUS"));
        assertFalse(RentalStatus.contains("PENDING")); // Partial match
    }

    @Test
    void testContains_NullInput() {
        assertFalse(RentalStatus.contains(null));
    }

    @Test
    void testFromString_ValidStatus() {
        assertEquals(Optional.of(RentalStatus.PENDING_APPROVAL), RentalStatus.fromString("PENDING_APPROVAL"));
        assertEquals(Optional.of(RentalStatus.PENDING_APPROVAL), RentalStatus.fromString("pending_approval"));
        assertEquals(Optional.of(RentalStatus.APPROVED), RentalStatus.fromString("APPROVED"));
        assertEquals(Optional.of(RentalStatus.ACTIVE), RentalStatus.fromString("active"));
    }

    @Test
    void testFromString_InvalidStatus() {
        assertEquals(Optional.empty(), RentalStatus.fromString("INVALID"));
    }

    @Test
    void testFromString_NullInput() {
        assertEquals(Optional.empty(), RentalStatus.fromString(null));
    }

    @Test
    void testEnumValues() {
        assertEquals("PENDING_APPROVAL", RentalStatus.PENDING_APPROVAL.name());
        assertEquals(RentalStatus.PENDING_APPROVAL, RentalStatus.valueOf("PENDING_APPROVAL"));
    }
}