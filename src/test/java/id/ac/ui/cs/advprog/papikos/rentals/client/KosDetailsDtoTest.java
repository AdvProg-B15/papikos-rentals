package id.ac.ui.cs.advprog.papikos.rentals.client;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class KosDetailsDtoTest {

    @Test
    void testKosDetailsDto_NoArgsConstructor() {
        KosDetailsDto dto = new KosDetailsDto();
        assertNull(dto.getId());
        assertNull(dto.getOwnerUserId());
        assertNull(dto.getName());
        assertNull(dto.getAddress());
        assertNull(dto.getDescription());
        assertEquals(0, dto.getNumRooms());
        assertFalse(dto.isListed());
        assertNull(dto.getMonthlyRentPrice());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getUpdatedAt());
    }

    @Test
    void testKosDetailsDto_AllArgsConstructorAndGettersSetters() {
        UUID id = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        String name = "Kos A";
        String address = "Address A";
        String description = "Description A";
        int numRooms = 10;
        boolean isListed = true;
        BigDecimal monthlyRentPrice = new BigDecimal("1000000");
        Date createdAt = new Date();
        Date updatedAt = new Date();

        KosDetailsDto dto = new KosDetailsDto(id, ownerUserId, name, address, description, numRooms, isListed, monthlyRentPrice, createdAt, updatedAt);

        assertEquals(id, dto.getId());
        assertEquals(ownerUserId, dto.getOwnerUserId());
        assertEquals(name, dto.getName());
        assertEquals(address, dto.getAddress());
        assertEquals(description, dto.getDescription());
        assertEquals(numRooms, dto.getNumRooms());
        assertTrue(dto.isListed());
        assertEquals(monthlyRentPrice, dto.getMonthlyRentPrice());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());

        // Test setters
        UUID newId = UUID.randomUUID();
        dto.setId(newId);
        assertEquals(newId, dto.getId());

        UUID newOwnerUserId = UUID.randomUUID();
        dto.setOwnerUserId(newOwnerUserId);
        assertEquals(newOwnerUserId, dto.getOwnerUserId());

        dto.setName("New Name");
        assertEquals("New Name", dto.getName());

        dto.setAddress("New Address");
        assertEquals("New Address", dto.getAddress());

        dto.setDescription("New Description");
        assertEquals("New Description", dto.getDescription());

        dto.setNumRooms(5);
        assertEquals(5, dto.getNumRooms());

        dto.setListed(false);
        assertFalse(dto.isListed());

        dto.setMonthlyRentPrice(new BigDecimal("2000000"));
        assertEquals(new BigDecimal("2000000"), dto.getMonthlyRentPrice());

        Date newDate = new Date(System.currentTimeMillis() + 10000);
        dto.setCreatedAt(newDate);
        assertEquals(newDate, dto.getCreatedAt());

        dto.setUpdatedAt(newDate);
        assertEquals(newDate, dto.getUpdatedAt());
    }
}
