package id.ac.ui.cs.advprog.papikos.rentals.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class UpdateRentalSubmissionRequestTest {

    @Test
    void testUpdateRentalSubmissionRequest_SettersAndGetters() {
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        String tenantName = "Updated John Doe";
        String tenantPhone = "081111111111";
        LocalDate startDate = LocalDate.now().plusMonths(1);
        Integer duration = 3;

        request.setSubmittedTenantName(tenantName);
        request.setSubmittedTenantPhone(tenantPhone);
        request.setRentalStartDate(startDate);
        request.setRentalDurationMonths(duration);

        assertEquals(tenantName, request.getSubmittedTenantName());
        assertEquals(tenantPhone, request.getSubmittedTenantPhone());
        assertEquals(startDate, request.getRentalStartDate());
        assertEquals(duration, request.getRentalDurationMonths());
    }

    @Test
    void testUpdateRentalSubmissionRequest_NoArgsConstructor() {
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        assertNull(request.getSubmittedTenantName());
        assertNull(request.getSubmittedTenantPhone());
        assertNull(request.getRentalStartDate());
        assertNull(request.getRentalDurationMonths());
    }
}