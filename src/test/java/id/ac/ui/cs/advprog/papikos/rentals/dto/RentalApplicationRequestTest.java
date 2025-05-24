package id.ac.ui.cs.advprog.papikos.rentals.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RentalApplicationRequestTest {

    @Test
    void testRentalApplicationRequest_SettersAndGetters() {
        RentalApplicationRequest request = new RentalApplicationRequest();
        UUID kosId = UUID.randomUUID();
        String tenantName = "John Doe";
        String tenantPhone = "081234567890";
        LocalDate startDate = LocalDate.now().plusDays(1);
        Integer duration = 12;

        request.setKosId(kosId);
        request.setSubmittedTenantName(tenantName);
        request.setSubmittedTenantPhone(tenantPhone);
        request.setRentalStartDate(startDate);
        request.setRentalDurationMonths(duration);

        assertEquals(kosId, request.getKosId());
        assertEquals(tenantName, request.getSubmittedTenantName());
        assertEquals(tenantPhone, request.getSubmittedTenantPhone());
        assertEquals(startDate, request.getRentalStartDate());
        assertEquals(duration, request.getRentalDurationMonths());
    }

    @Test
    void testRentalApplicationRequest_NoArgsConstructor() {
        RentalApplicationRequest request = new RentalApplicationRequest();
        assertNull(request.getKosId());
        assertNull(request.getSubmittedTenantName());
        assertNull(request.getSubmittedTenantPhone());
        assertNull(request.getRentalStartDate());
        assertNull(request.getRentalDurationMonths());
    }
}