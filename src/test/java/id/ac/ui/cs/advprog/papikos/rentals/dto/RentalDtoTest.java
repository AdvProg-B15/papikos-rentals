package id.ac.ui.cs.advprog.papikos.rentals.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RentalDtoTest {

    @Test
    void testRentalDto_BuilderAndGetters() {
        UUID rentalId = UUID.randomUUID();
        UUID tenantUserId = UUID.randomUUID();
        UUID kosId = UUID.randomUUID();
        UUID ownerUserId = UUID.randomUUID();
        String kosName = "Kos Indah";
        String tenantName = "Jane Doe";
        String tenantPhone = "089876543210";
        LocalDate startDate = LocalDate.now();
        int duration = 6;
        LocalDate endDate = startDate.plusMonths(duration);
        String status = "APPROVED";
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        RentalDto dto = RentalDto.builder()
                .rentalId(rentalId)
                .tenantUserId(tenantUserId)
                .kosId(kosId)
                .ownerUserId(ownerUserId)
                .kosName(kosName)
                .submittedTenantName(tenantName)
                .submittedTenantPhone(tenantPhone)
                .rentalStartDate(startDate)
                .rentalDurationMonths(duration)
                .rentalEndDate(endDate)
                .status(status)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        assertEquals(rentalId, dto.getRentalId());
        assertEquals(tenantUserId, dto.getTenantUserId());
        assertEquals(kosId, dto.getKosId());
        assertEquals(ownerUserId, dto.getOwnerUserId());
        assertEquals(kosName, dto.getKosName());
        assertEquals(tenantName, dto.getSubmittedTenantName());
        assertEquals(tenantPhone, dto.getSubmittedTenantPhone());
        assertEquals(startDate, dto.getRentalStartDate());
        assertEquals(duration, dto.getRentalDurationMonths());
        assertEquals(endDate, dto.getRentalEndDate());
        assertEquals(status, dto.getStatus());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
    }

    @Test
    void testRentalDto_Setters() {
        RentalDto dto = RentalDto.builder().build(); // Start with an empty DTO

        UUID rentalId = UUID.randomUUID();
        dto.setRentalId(rentalId);
        assertEquals(rentalId, dto.getRentalId());

        UUID tenantUserId = UUID.randomUUID();
        dto.setTenantUserId(tenantUserId);
        assertEquals(tenantUserId, dto.getTenantUserId());

        UUID kosId = UUID.randomUUID();
        dto.setKosId(kosId);
        assertEquals(kosId, dto.getKosId());

        UUID ownerUserId = UUID.randomUUID();
        dto.setOwnerUserId(ownerUserId);
        assertEquals(ownerUserId, dto.getOwnerUserId());

        String kosName = "Kos Damai";
        dto.setKosName(kosName);
        assertEquals(kosName, dto.getKosName());

        String tenantName = "John Smith";
        dto.setSubmittedTenantName(tenantName);
        assertEquals(tenantName, dto.getSubmittedTenantName());

        String tenantPhone = "123456789";
        dto.setSubmittedTenantPhone(tenantPhone);
        assertEquals(tenantPhone, dto.getSubmittedTenantPhone());

        LocalDate startDate = LocalDate.now().plusDays(5);
        dto.setRentalStartDate(startDate);
        assertEquals(startDate, dto.getRentalStartDate());

        int duration = 3;
        dto.setRentalDurationMonths(duration);
        assertEquals(duration, dto.getRentalDurationMonths());

        LocalDate endDate = startDate.plusMonths(duration);
        dto.setRentalEndDate(endDate);
        assertEquals(endDate, dto.getRentalEndDate());

        String status = "PENDING";
        dto.setStatus(status);
        assertEquals(status, dto.getStatus());

        LocalDateTime createdAt = LocalDateTime.now().minusHours(2);
        dto.setCreatedAt(createdAt);
        assertEquals(createdAt, dto.getCreatedAt());

        LocalDateTime updatedAt = LocalDateTime.now();
        dto.setUpdatedAt(updatedAt);
        assertEquals(updatedAt, dto.getUpdatedAt());
    }
}