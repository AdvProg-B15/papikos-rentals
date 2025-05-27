package id.ac.ui.cs.advprog.papikos.rentals.controller;

import id.ac.ui.cs.advprog.papikos.rentals.client.KosServiceClient;
import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalApplicationRequest;
import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalDto;
import id.ac.ui.cs.advprog.papikos.rentals.dto.UpdateRentalSubmissionRequest;
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.response.ApiResponse;
import id.ac.ui.cs.advprog.papikos.rentals.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalControllerTest {

    @Mock
    private RentalService rentalService;

    @Mock
    private KosServiceClient kosServiceClient;

    @InjectMocks
    private RentalController rentalController;

    private UUID testTenantUserId;
    private UUID testOwnerUserId;
    private UUID testRentalId;
    private UUID testKosId;
    private RentalDto rentalDto;
    private Authentication tenantAuth;
    private Authentication ownerAuth;
    private LocalDate today;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        testTenantUserId = UUID.randomUUID();
        testOwnerUserId = UUID.randomUUID();
        testRentalId = UUID.randomUUID();
        testKosId = UUID.randomUUID();
        today = LocalDate.now();
        now = LocalDateTime.now();

        tenantAuth = new UsernamePasswordAuthenticationToken(
                testTenantUserId.toString(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("TENANT"))
        );

        ownerAuth = new UsernamePasswordAuthenticationToken(
                testOwnerUserId.toString(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("OWNER"))
        );

        rentalDto = RentalDto.builder()
                .rentalId(testRentalId)
                .kosId(testKosId)
                .tenantUserId(testTenantUserId)
                .ownerUserId(testOwnerUserId) // Different owner for distinction if needed
                .kosName("Test Kos Name")
                .submittedTenantName("Test Tenant")
                .submittedTenantPhone("08123456789")
                .rentalStartDate(today.plusDays(1))
                .rentalDurationMonths(1)
                .rentalEndDate(today.plusDays(1).plusMonths(1))
                .status(RentalStatus.PENDING_APPROVAL.toString())
                .createdAt(now.minusHours(1))
                .updatedAt(now)
                .build();
    }

    @Test
    void getUserIdFromAuthentication_validAuth_returnsUUID() {
        UUID extractedId = rentalController.getUserIdFromAuthentication(tenantAuth);
        assertEquals(testTenantUserId, extractedId);
    }

    @Test
    void getUserIdFromAuthentication_nullAuth_throwsIllegalStateException() {
        Exception exception = assertThrows(IllegalStateException.class,
                () -> rentalController.getUserIdFromAuthentication(null));
        assertEquals("Authentication principal is required but missing.", exception.getMessage());
    }

    @Test
    void getUserIdFromAuthentication_invalidUUIDFormat_throwsIllegalArgumentException() {
        Authentication invalidAuth = new UsernamePasswordAuthenticationToken("not-a-uuid", null, Collections.singletonList(new SimpleGrantedAuthority("TENANT")));
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> rentalController.getUserIdFromAuthentication(invalidAuth));
        assertEquals("Invalid user identifier format in authentication token.", exception.getMessage());
    }


    @Test
    void testEndpoint_Success() {
        String kosIdString = testKosId.toString();
        doNothing().when(rentalService).tryFetchKosDetail(testKosId);

        ResponseEntity<ApiResponse<String>> responseEntity = rentalController.testEndpoint(kosIdString);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<String> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Test successful", apiResponse.getMessage());
        assertEquals("SUCCESS", apiResponse.getData());
        verify(rentalService, times(1)).tryFetchKosDetail(testKosId);
    }

    @Test
    void submitRentalApplication_Success_Returns201() {
        RentalApplicationRequest request = new RentalApplicationRequest();
        request.setKosId(testKosId);
        request.setSubmittedTenantName("Test Tenant");
        request.setSubmittedTenantPhone("08123456789");
        request.setRentalStartDate(today.plusDays(1));
        request.setRentalDurationMonths(1);

        when(rentalService.submitRentalApplication(eq(testTenantUserId), any(RentalApplicationRequest.class)))
                .thenReturn(rentalDto);

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.submitRentalApplication(tenantAuth, request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        ApiResponse<RentalDto> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.CREATED.value(), apiResponse.getStatus());
        assertEquals("Rental application submitted successfully", apiResponse.getMessage());
        assertSame(rentalDto, apiResponse.getData());
        verify(rentalService, times(1)).submitRentalApplication(eq(testTenantUserId), eq(request));
    }

    @Test
    void getMyRentals_Success_Returns200() {
        List<RentalDto> rentalList = Collections.singletonList(rentalDto);
        when(rentalService.getTenantRentals(eq(testTenantUserId))).thenReturn(rentalList);

        ResponseEntity<ApiResponse<List<RentalDto>>> responseEntity =
                rentalController.getMyRentals(tenantAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<List<RentalDto>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Tenant's rentals fetched successfully", apiResponse.getMessage());
        assertSame(rentalList, apiResponse.getData());
        verify(rentalService, times(1)).getTenantRentals(eq(testTenantUserId));
    }

    @Test
    void getOwnerRentals_NoFilters_Success_Returns200() {
        List<RentalDto> rentalList = Collections.singletonList(rentalDto);
        // Adjust owner ID in rentalDto for this test or use a different rentalDto instance
        rentalDto.setOwnerUserId(testOwnerUserId);
        when(rentalService.getOwnerRentals(eq(testOwnerUserId), eq(null), eq(null))).thenReturn(rentalList);

        ResponseEntity<ApiResponse<List<RentalDto>>> responseEntity =
                rentalController.getOwnerRentals(ownerAuth, null, null);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<List<RentalDto>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Owner's rentals fetched successfully", apiResponse.getMessage());
        assertSame(rentalList, apiResponse.getData());
        verify(rentalService, times(1)).getOwnerRentals(eq(testOwnerUserId), eq(null), eq(null));
    }

    @Test
    void getOwnerRentals_WithFilters_Success_Returns200() {
        List<RentalDto> rentalList = Collections.singletonList(rentalDto);
        rentalDto.setOwnerUserId(testOwnerUserId); // Ensure owner matches
        RentalStatus statusFilter = RentalStatus.PENDING_APPROVAL;
        when(rentalService.getOwnerRentals(eq(testOwnerUserId), eq(statusFilter), eq(testKosId))).thenReturn(rentalList);

        ResponseEntity<ApiResponse<List<RentalDto>>> responseEntity =
                rentalController.getOwnerRentals(ownerAuth, statusFilter, testKosId);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<List<RentalDto>> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Owner's rentals fetched successfully", apiResponse.getMessage());
        assertSame(rentalList, apiResponse.getData());
        verify(rentalService, times(1)).getOwnerRentals(eq(testOwnerUserId), eq(statusFilter), eq(testKosId));
    }

    @Test
    void getRentalById_TenantAccess_Success_Returns200() {
        when(rentalService.getRentalById(eq(testRentalId), eq(testTenantUserId))).thenReturn(rentalDto);

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.getRentalById(testRentalId, tenantAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<RentalDto> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Rental details fetched successfully", apiResponse.getMessage());
        assertSame(rentalDto, apiResponse.getData());
        verify(rentalService, times(1)).getRentalById(eq(testRentalId), eq(testTenantUserId));
    }

    @Test
    void getRentalById_OwnerAccess_Success_Returns200() {
        // Assume rentalDto's ownerUserId is testOwnerUserId for this scenario
        rentalDto.setOwnerUserId(testOwnerUserId);
        when(rentalService.getRentalById(eq(testRentalId), eq(testOwnerUserId))).thenReturn(rentalDto);

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.getRentalById(testRentalId, ownerAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // ... (rest of assertions similar to TenantAccess)
        verify(rentalService, times(1)).getRentalById(eq(testRentalId), eq(testOwnerUserId));
    }


    @Test
    void editRentalSubmission_Success_Returns200() {
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        request.setSubmittedTenantName("Updated Name");
        request.setRentalDurationMonths(2);

        when(rentalService.editRentalSubmission(eq(testRentalId), eq(testTenantUserId), any(UpdateRentalSubmissionRequest.class)))
                .thenReturn(rentalDto); // Assume rentalDto is the updated one

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.editRentalSubmission(testRentalId, tenantAuth, request);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<RentalDto> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Rental submission updated successfully", apiResponse.getMessage());
        assertSame(rentalDto, apiResponse.getData());
        verify(rentalService, times(1)).editRentalSubmission(eq(testRentalId), eq(testTenantUserId), eq(request));
    }

    @Test
    void cancelRental_Tenant_Success_Returns200() {
        when(rentalService.cancelRental(eq(testRentalId), eq(testTenantUserId), eq("TENANT")))
                .thenReturn(rentalDto); // Assume rentalDto is the cancelled one

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.cancelRental(testRentalId, tenantAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<RentalDto> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Rental cancelled successfully", apiResponse.getMessage());
        assertSame(rentalDto, apiResponse.getData());
        verify(rentalService, times(1)).cancelRental(eq(testRentalId), eq(testTenantUserId), eq("TENANT"));
    }

    @Test
    void cancelRental_Owner_Success_Returns200() {
        when(rentalService.cancelRental(eq(testRentalId), eq(testOwnerUserId), eq("OWNER")))
                .thenReturn(rentalDto); // Assume rentalDto is the cancelled one

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.cancelRental(testRentalId, ownerAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<RentalDto> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Rental cancelled successfully", apiResponse.getMessage());
        assertSame(rentalDto, apiResponse.getData());
        verify(rentalService, times(1)).cancelRental(eq(testRentalId), eq(testOwnerUserId), eq("OWNER"));
    }

    @Test
    void approveRental_Success_Returns200() {
        when(rentalService.approveRental(eq(testRentalId), eq(testOwnerUserId)))
                .thenReturn(rentalDto); // Assume rentalDto is the approved one

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.approveRental(testRentalId, ownerAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<RentalDto> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Rental approved successfully", apiResponse.getMessage());
        assertSame(rentalDto, apiResponse.getData());
        verify(rentalService, times(1)).approveRental(eq(testRentalId), eq(testOwnerUserId));
    }

    @Test
    void rejectRental_Success_Returns200() {
        when(rentalService.rejectRental(eq(testRentalId), eq(testOwnerUserId)))
                .thenReturn(rentalDto); // Assume rentalDto is the rejected one

        ResponseEntity<ApiResponse<RentalDto>> responseEntity =
                rentalController.rejectRental(testRentalId, ownerAuth);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        ApiResponse<RentalDto> apiResponse = responseEntity.getBody();
        assertNotNull(apiResponse);
        assertEquals(HttpStatus.OK.value(), apiResponse.getStatus());
        assertEquals("Rental rejected successfully", apiResponse.getMessage());
        assertSame(rentalDto, apiResponse.getData());
        verify(rentalService, times(1)).rejectRental(eq(testRentalId), eq(testOwnerUserId));
    }
}