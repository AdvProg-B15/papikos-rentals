package id.ac.ui.cs.advprog.papikos.rentals.dto;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class RentalEventTest {

    @Test
    void testRentalEvent_NoArgsConstructorAndSettersGetters() {
        RentalEvent event = new RentalEvent();

        String rentalId = "rental123";
        String userId = "user456";
        String kosId = "kos789";
        String kosOwnerId = "owner101";
        String kosName = "Kos Ceria";
        LocalDate bookingDate = LocalDate.now();
        Integer rentalDurationMonths = 12;
        BigDecimal price = new BigDecimal("500.00");
        String status = "CONFIRMED";
        String tenantName = "Alice Wonderland";
        String tenantPhone = "081122334455";
        String timestamp = "2023-01-01T10:00:00";

        event.setRentalId(rentalId);
        event.setUserId(userId);
        event.setKosId(kosId);
        event.setKosOwnerId(kosOwnerId);
        event.setKosName(kosName);
        event.setBookingDate(bookingDate);
        event.setRentalDurationMonths(rentalDurationMonths);
        event.setPrice(price);
        event.setStatus(status);
        event.setTenantName(tenantName);
        event.setTenantPhone(tenantPhone);
        event.setTimestamp(timestamp);

        assertEquals(rentalId, event.getRentalId());
        assertEquals(userId, event.getUserId());
        assertEquals(kosId, event.getKosId());
        assertEquals(kosOwnerId, event.getKosOwnerId());
        assertEquals(kosName, event.getKosName());
        assertEquals(bookingDate, event.getBookingDate());
        assertEquals(rentalDurationMonths, event.getRentalDurationMonths());
        assertEquals(price, event.getPrice());
        assertEquals(status, event.getStatus());
        assertEquals(tenantName, event.getTenantName());
        assertEquals(tenantPhone, event.getTenantPhone());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    void testRentalEvent_AllArgsConstructor() {
        String rentalId = "rental123";
        String userId = "user456";
        String kosId = "kos789";
        String kosOwnerId = "owner101";
        String kosName = "Kos Ceria";
        LocalDate bookingDate = LocalDate.now();
        Integer rentalDurationMonths = 12;
        BigDecimal price = new BigDecimal("500.00");
        String status = "CONFIRMED";
        String tenantName = "Alice Wonderland";
        String tenantPhone = "081122334455";
        String timestamp = "2023-01-01T10:00:00";

        RentalEvent event = new RentalEvent(rentalId, userId, kosId, kosOwnerId, kosName,
                bookingDate, rentalDurationMonths, price, status,
                tenantName, tenantPhone, timestamp);

        assertEquals(rentalId, event.getRentalId());
        assertEquals(userId, event.getUserId());
        assertEquals(kosId, event.getKosId());
        assertEquals(kosOwnerId, event.getKosOwnerId());
        assertEquals(kosName, event.getKosName());
        assertEquals(bookingDate, event.getBookingDate());
        assertEquals(rentalDurationMonths, event.getRentalDurationMonths());
        assertEquals(price, event.getPrice());
        assertEquals(status, event.getStatus());
        assertEquals(tenantName, event.getTenantName());
        assertEquals(tenantPhone, event.getTenantPhone());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    void testRentalEvent_Builder() {
        String rentalId = "rental123";
        String userId = "user456";
        String kosId = "kos789";
        String kosOwnerId = "owner101";
        String kosName = "Kos Ceria";
        LocalDate bookingDate = LocalDate.now();
        Integer rentalDurationMonths = 12;
        BigDecimal price = new BigDecimal("500.00");
        String status = "CONFIRMED";
        String tenantName = "Alice Wonderland";
        String tenantPhone = "081122334455";
        String timestamp = "2023-01-01T10:00:00";

        RentalEvent event = RentalEvent.builder()
                .rentalId(rentalId)
                .userId(userId)
                .kosId(kosId)
                .kosOwnerId(kosOwnerId)
                .kosName(kosName)
                .bookingDate(bookingDate)
                .rentalDurationMonths(rentalDurationMonths)
                .price(price)
                .status(status)
                .tenantName(tenantName)
                .tenantPhone(tenantPhone)
                .timestamp(timestamp)
                .build();

        assertEquals(rentalId, event.getRentalId());
        assertEquals(userId, event.getUserId());
        assertEquals(kosId, event.getKosId());
        assertEquals(kosOwnerId, event.getKosOwnerId());
        assertEquals(kosName, event.getKosName());
        assertEquals(bookingDate, event.getBookingDate());
        assertEquals(rentalDurationMonths, event.getRentalDurationMonths());
        assertEquals(price, event.getPrice());
        assertEquals(status, event.getStatus());
        assertEquals(tenantName, event.getTenantName());
        assertEquals(tenantPhone, event.getTenantPhone());
        assertEquals(timestamp, event.getTimestamp());
    }
}