package id.ac.ui.cs.advprog.papikos.rentals.service;

import id.ac.ui.cs.advprog.papikos.rentals.client.*;
import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalApplicationRequest;
import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalDto;
import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalEvent;
import id.ac.ui.cs.advprog.papikos.rentals.dto.UpdateRentalSubmissionRequest;
import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.exception.ForbiddenException;
import id.ac.ui.cs.advprog.papikos.rentals.exception.ResourceNotFoundException;
import id.ac.ui.cs.advprog.papikos.rentals.exception.ValidationException;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import id.ac.ui.cs.advprog.papikos.rentals.repository.RentalRepository;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate; // Tidak digunakan secara langsung di metode yang dites, tapi ada sebagai field
import com.fasterxml.jackson.databind.ObjectMapper; // Tidak digunakan secara langsung di metode yang dites, tapi ada sebagai field

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private KosServiceClient kosServiceClient;
    @Mock
    private NotificationServiceClient notificationServiceClient;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private UUID kosId;
    private UUID tenantUserId;
    private UUID ownerUserId;
    private UUID rentalId;
    private KosDetailsDto mockKosDetailsDto;
    private Rental mockRental;
    private KosApiResponseWrapper<KosDetailsDto> mockKosApiResponse;

    @BeforeEach
    void setUp() {
        kosId = UUID.randomUUID();
        tenantUserId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        rentalId = UUID.randomUUID();

        mockKosDetailsDto = new KosDetailsDto();
        mockKosDetailsDto.setId(kosId);
        mockKosDetailsDto.setName("Test Kos");
        mockKosDetailsDto.setOwnerUserId(ownerUserId);
        mockKosDetailsDto.setListed(true);
        mockKosDetailsDto.setNumRooms(5);
        mockKosDetailsDto.setMonthlyRentPrice(new BigDecimal("1000000"));

        mockKosApiResponse = new KosApiResponseWrapper<>();
        mockKosApiResponse.setData(mockKosDetailsDto);
        mockKosApiResponse.setStatus(HttpStatus.OK.value());
        mockKosApiResponse.setMessage("Success");

        mockRental = new Rental();
        mockRental.setId(rentalId);
        mockRental.setKosId(kosId);
        mockRental.setTenantUserId(tenantUserId);
        mockRental.setOwnerUserId(ownerUserId);
        mockRental.setStatus(RentalStatus.PENDING_APPROVAL);
        mockRental.setRentalStartDate(LocalDate.now().plusDays(1));
        mockRental.setRentalDurationMonths(1);
        mockRental.setSubmittedTenantName("Test Tenant");
        mockRental.setSubmittedTenantPhone("08123");
        mockRental.setCreatedAt(LocalDateTime.now());
        mockRental.setUpdatedAt(LocalDateTime.now());
        if (mockRental.getRentalStartDate() != null && mockRental.getRentalDurationMonths() != null) {
            mockRental.setRentalEndDate(mockRental.getRentalStartDate().plusMonths(mockRental.getRentalDurationMonths()));
        }
    }

    @Test
    void fetchKosDetails_Success() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        KosDetailsDto result = rentalService.fetchKosDetails(kosId);
        assertNotNull(result);
        assertEquals(mockKosDetailsDto.getName(), result.getName());
        verify(kosServiceClient).getKosDetailsApiResponse(kosId);
    }

    @Test
    void fetchKosDetails_FeignExceptionNotFound() {
        Request dummyRequest = Request.create(Request.HttpMethod.GET, "/dummy", Collections.emptyMap(), null, new RequestTemplate());
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, null));
        assertThrows(ResourceNotFoundException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_FeignExceptionOther() {
        Request dummyRequest = Request.create(Request.HttpMethod.GET, "/dummy", Collections.emptyMap(), null, new RequestTemplate());
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(new FeignException.InternalServerError("Server Error", dummyRequest, null, null));
        assertThrows(RentalServiceImpl.ServiceUnavailableException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_NullResponseWrapper() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(null);
        assertThrows(RentalServiceImpl.ServiceUnavailableException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_NonOkStatusInWrapper_NotFound() {
        mockKosApiResponse.setStatus(HttpStatus.NOT_FOUND.value());
        mockKosApiResponse.setMessage("Kos entity not found");
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        assertThrows(ResourceNotFoundException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_NonOkStatusInWrapper_Other() {
        mockKosApiResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        mockKosApiResponse.setMessage("Internal error in Kos service");
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        assertThrows(RentalServiceImpl.ServiceInteractionException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_NullDataInWrapper() {
        mockKosApiResponse.setData(null);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        assertThrows(ResourceNotFoundException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void tryFetchKosDetail_Success() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        assertDoesNotThrow(() -> rentalService.tryFetchKosDetail(kosId));
        verify(kosServiceClient).getKosDetailsApiResponse(kosId);
    }

    @Test
    void tryFetchKosDetail_Failure() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(new FeignException.NotFound("Not Found", Request.create(Request.HttpMethod.GET, "/dummy", Collections.emptyMap(), null, new RequestTemplate()), null, null));
        assertThrows(RentalServiceImpl.ServiceUnavailableException.class, () -> rentalService.tryFetchKosDetail(kosId));
    }


    // --- Tests for submitRentalApplication ---
    @Test
    void submitRentalApplication_Success() {
        RentalApplicationRequest request = new RentalApplicationRequest();
        request.setKosId(kosId);
        request.setSubmittedTenantName("Tenant A");
        request.setSubmittedTenantPhone("08111");
        request.setRentalStartDate(LocalDate.now().plusDays(1));
        request.setRentalDurationMonths(1);

        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class));

        RentalDto result = rentalService.submitRentalApplication(tenantUserId, request);

        assertNotNull(result);
        assertEquals(mockRental.getId(), result.getRentalId());
        verify(rentalRepository).save(any(Rental.class));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class));
    }

    @Test
    void submitRentalApplication_KosNotListed_ThrowsValidationException() {
        mockKosDetailsDto.setListed(false);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        RentalApplicationRequest request = new RentalApplicationRequest();
        request.setKosId(kosId);

        assertThrows(ValidationException.class, () -> rentalService.submitRentalApplication(tenantUserId, request));
    }

    @Test
    void submitRentalApplication_NoRoomsAvailable_ThrowsValidationException() {
        mockKosDetailsDto.setNumRooms(1); // Only 1 room
        Rental existingRental = new Rental(); // Mock an existing active/approved rental
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(List.of(existingRental));
        RentalApplicationRequest request = new RentalApplicationRequest();
        request.setKosId(kosId);

        assertThrows(ValidationException.class, () -> rentalService.submitRentalApplication(tenantUserId, request));
    }

    @Test
    void submitRentalApplication_NotificationFails_StillSubmitsAndLogsError() {
        RentalApplicationRequest request = new RentalApplicationRequest();
        request.setKosId(kosId);
        request.setSubmittedTenantName("Tenant B");
        request.setSubmittedTenantPhone("08222");
        request.setRentalStartDate(LocalDate.now().plusDays(1));
        request.setRentalDurationMonths(1);

        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);

        Request dummyFeignRequest = Request.create(Request.HttpMethod.POST, "http://dummy", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenThrow(new FeignException.InternalServerError("Notification service error", dummyFeignRequest, null, null));

        assertThrows(FeignException.class, () -> rentalService.submitRentalApplication(tenantUserId, request));

        verify(rentalRepository).save(any(Rental.class));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(RentalEvent.class));
    }

    @Test
    void submitRentalApplication_RabbitMQFails_StillSubmitsAndLogsError() {
        RentalApplicationRequest request = new RentalApplicationRequest();
        // ... setup request ...
        request.setKosId(kosId);
        request.setSubmittedTenantName("Tenant C");
        request.setSubmittedTenantPhone("08333");
        request.setRentalStartDate(LocalDate.now().plusDays(1));
        request.setRentalDurationMonths(1);

        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());
        doThrow(new RuntimeException("RabbitMQ error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class));

        RentalDto result = rentalService.submitRentalApplication(tenantUserId, request); // Exception from RabbitMQ is caught and logged

        assertNotNull(result);
        assertEquals(mockRental.getId(), result.getRentalId());
        verify(rentalRepository).save(any(Rental.class));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class)); // Attempted
    }


    // --- Tests for getTenantRentals ---
    @Test
    void getTenantRentals_Success() {
        when(rentalRepository.findByTenantUserId(tenantUserId)).thenReturn(List.of(mockRental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse); // For getKosName

        List<RentalDto> results = rentalService.getTenantRentals(tenantUserId);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(mockRental.getId(), results.get(0).getRentalId());
        assertEquals(mockKosDetailsDto.getName(), results.get(0).getKosName());
    }

    @Test
    void getOwnerRentals_NoFilter_Success() {
        when(rentalRepository.findByOwnerUserId(ownerUserId)).thenReturn(List.of(mockRental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse); // For getKosName

        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, null, null);

        assertFalse(results.isEmpty());
        assertEquals(mockRental.getId(), results.get(0).getRentalId());
    }

    @Test
    void getOwnerRentals_WithKosIdFilter_Success() {
        mockRental.setOwnerUserId(ownerUserId); // Ensure this rental belongs to the owner
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse); // For fetchKosDetails and getKosName
        when(rentalRepository.findAll()).thenReturn(List.of(mockRental)); // Mocking findAll behavior

        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, null, kosId);

        assertFalse(results.isEmpty());
        assertEquals(mockRental.getId(), results.get(0).getRentalId());
        assertEquals(kosId, results.get(0).getKosId());
    }

    @Test
    void getOwnerRentals_WithKosIdFilter_Forbidden() {
        UUID otherKosId = UUID.randomUUID();
        KosDetailsDto otherKosDetails = new KosDetailsDto();
        otherKosDetails.setId(otherKosId);
        otherKosDetails.setOwnerUserId(UUID.randomUUID());

        KosApiResponseWrapper<KosDetailsDto> otherKosApiResponse = new KosApiResponseWrapper<>();
        otherKosApiResponse.setData(otherKosDetails);
        otherKosApiResponse.setStatus(HttpStatus.OK.value());

        when(kosServiceClient.getKosDetailsApiResponse(otherKosId)).thenReturn(otherKosApiResponse);

        assertThrows(ForbiddenException.class, () -> rentalService.getOwnerRentals(ownerUserId, null, otherKosId));
    }

    @Test
    void getOwnerRentals_WithStatusFilter_Success() {
        mockRental.setStatus(RentalStatus.PENDING_APPROVAL);
        when(rentalRepository.findByOwnerUserId(ownerUserId)).thenReturn(List.of(mockRental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);

        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, RentalStatus.PENDING_APPROVAL, null);
        assertFalse(results.isEmpty());
        assertEquals(RentalStatus.PENDING_APPROVAL.toString(), results.get(0).getStatus());
    }


    @Test
    void getRentalById_TenantAccess_Success() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);

        RentalDto result = rentalService.getRentalById(rentalId, tenantUserId);
        assertNotNull(result);
        assertEquals(mockRental.getId(), result.getRentalId());
    }

    @Test
    void getRentalById_OwnerAccess_Success() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);

        RentalDto result = rentalService.getRentalById(rentalId, ownerUserId);
        assertNotNull(result);
    }

    @Test
    void getRentalById_NotFound_ThrowsResourceNotFoundException() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rentalService.getRentalById(rentalId, tenantUserId));
    }

    @Test
    void getRentalById_Forbidden() {
        UUID otherUserId = UUID.randomUUID();
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));

        assertThrows(ForbiddenException.class, () -> rentalService.getRentalById(rentalId, otherUserId));
    }

    @Test
    void editRentalSubmission_Success() {
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        request.setSubmittedTenantName("Updated Tenant Name");
        request.setRentalDurationMonths(2);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);

        RentalDto result = rentalService.editRentalSubmission(rentalId, tenantUserId, request);

        assertNotNull(result);
        assertEquals("Updated Tenant Name", result.getSubmittedTenantName());
        assertEquals(2, result.getRentalDurationMonths());
        verify(rentalRepository).save(mockRental);
    }

    @Test
    void editRentalSubmission_PartialUpdate_Success() {
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        request.setRentalDurationMonths(3);

        mockRental.setStatus(RentalStatus.PENDING_APPROVAL);
        mockRental.setTenantUserId(tenantUserId);
        mockRental.setSubmittedTenantName("Original Name");

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0)); // return the argument
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);

        RentalDto result = rentalService.editRentalSubmission(rentalId, tenantUserId, request);

        assertNotNull(result);
        assertEquals(3, result.getRentalDurationMonths()); // Check updated field
        assertEquals("Original Name", result.getSubmittedTenantName()); // Check non-updated field remains
        verify(rentalRepository).save(argThat(savedRental ->
                savedRental.getRentalDurationMonths() == 3 &&
                        "Original Name".equals(savedRental.getSubmittedTenantName()) // Verify it was not nulled
        ));
    }


    @Test
    void editRentalSubmission_NotFound_ThrowsResourceNotFoundException() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        assertThrows(ResourceNotFoundException.class, () -> rentalService.editRentalSubmission(rentalId, tenantUserId, request));
    }

    @Test
    void editRentalSubmission_Forbidden_NotTenant() {
        UUID otherTenantId = UUID.randomUUID();
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        assertThrows(ForbiddenException.class, () -> rentalService.editRentalSubmission(rentalId, otherTenantId, request));
    }

    @Test
    void editRentalSubmission_WrongStatus_ThrowsValidationException() {
        mockRental.setStatus(RentalStatus.APPROVED); // Not PENDING_APPROVAL
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
        assertThrows(ValidationException.class, () -> rentalService.editRentalSubmission(rentalId, tenantUserId, request));
    }


    @Test
    void cancelRental_Tenant_Success() {
        mockRental.setStatus(RentalStatus.APPROVED); // Can be cancelled by tenant if approved
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse); // For getKosName
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        RentalDto result = rentalService.cancelRental(rentalId, tenantUserId, "TENANT");

        assertNotNull(result);
        assertEquals(RentalStatus.CANCELLED.toString(), result.getStatus());
        verify(rentalRepository).save(mockRental);
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void cancelRental_Forbidden_NotTenantOrWrongRole() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        assertThrows(ForbiddenException.class, () -> rentalService.cancelRental(rentalId, ownerUserId, "OWNER")); // Owner trying to cancel as tenant
        assertThrows(ForbiddenException.class, () -> rentalService.cancelRental(rentalId, tenantUserId, "OWNER")); // Tenant with wrong role string
    }

    @Test
    void cancelRental_WrongStatus_ThrowsValidationException() {
        mockRental.setStatus(RentalStatus.COMPLETED); // Cannot be cancelled by tenant
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        assertThrows(ValidationException.class, () -> rentalService.cancelRental(rentalId, tenantUserId, "TENANT"));
    }

    @Test
    void approveRental_Success() {
        mockRental.setStatus(RentalStatus.PENDING_APPROVAL);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList()); // No other active/approved rentals
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class));

        RentalDto result = rentalService.approveRental(rentalId, ownerUserId);

        assertNotNull(result);
        assertEquals(RentalStatus.APPROVED.toString(), result.getStatus());
        verify(rentalRepository).save(mockRental);
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class));
    }

    @Test
    void approveRental_Forbidden_NotOwner() {
        UUID otherOwnerId = UUID.randomUUID();
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental)); // mockRental owner is ownerUserId
        assertThrows(ForbiddenException.class, () -> rentalService.approveRental(rentalId, otherOwnerId));
    }

    @Test
    void approveRental_WrongStatus_ThrowsValidationException() {
        mockRental.setStatus(RentalStatus.APPROVED);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        assertThrows(ValidationException.class, () -> rentalService.approveRental(rentalId, ownerUserId));
    }

    @Test
    void approveRental_NoRoomsAvailable_ThrowsValidationException() {
        mockRental.setStatus(RentalStatus.PENDING_APPROVAL);
        mockKosDetailsDto.setNumRooms(0); // No rooms in Kos
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());


        assertThrows(ValidationException.class, () -> rentalService.approveRental(rentalId, ownerUserId));
    }

    @Test
    void rejectRental_Success() {
        mockRental.setStatus(RentalStatus.PENDING_APPROVAL);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(mockRental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(mockRental);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse); // For getKosName
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        RentalDto result = rentalService.rejectRental(rentalId, ownerUserId);

        assertNotNull(result);
        assertEquals(RentalStatus.REJECTED.toString(), result.getStatus());
        verify(rentalRepository).save(mockRental);
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }


    @Test
    void getKosName_Success() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse);
        String name = rentalService.getKosName(kosId);
        assertEquals(mockKosDetailsDto.getName(), name);
    }

    @Test
    void getKosName_ResourceNotFound_ReturnsPlaceholder() {
        Request dummyRequest = Request.create(Request.HttpMethod.GET, "/dummy", Collections.emptyMap(), null, new RequestTemplate());
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, null));
        String name = rentalService.getKosName(kosId);
        assertTrue(name.startsWith("Kos (ID: "));
    }

    @Test
    void getKosName_ServiceUnavailable_ReturnsPlaceholder() {
        Request dummyRequest = Request.create(Request.HttpMethod.GET, "/dummy", Collections.emptyMap(), null, new RequestTemplate());
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(new FeignException.ServiceUnavailable("Unavailable", dummyRequest, null, null));
        String name = rentalService.getKosName(kosId);
        assertTrue(name.startsWith("Kos (ID: "));
    }

    @Test
    void sendNotification_Success() {
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> rentalService.sendNotification(ownerUserId, "Title", "Message", rentalId, kosId));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void sendNotification_FeignException_ThrowsFeignException() {
        Request dummyRequest = Request.create(Request.HttpMethod.POST, "http://dummy", Collections.emptyMap(), null, StandardCharsets.UTF_8, null);
        FeignException mockFeignException = new FeignException.InternalServerError("Server Error", dummyRequest, "body".getBytes(), Collections.emptyMap());
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenThrow(mockFeignException);

        assertThrows(FeignException.class, () -> rentalService.sendNotification(ownerUserId, "Title", "Message", rentalId, kosId));
    }

    @Test
    void sendNotification_NonFeignException_ThrowsServiceInteractionException() {
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenThrow(new RuntimeException("Generic network error"));

        assertThrows(RentalServiceImpl.ServiceInteractionException.class,
                () -> rentalService.sendNotification(ownerUserId, "Title", "Message", rentalId, kosId));
    }

    @Test
    void sendNotification_NonSuccessfulResponse_LogsWarning() {
        when(notificationServiceClient.sendNotification(any(NotificationRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());


        assertDoesNotThrow(() -> rentalService.sendNotification(ownerUserId, "Title", "Message", rentalId, kosId));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }


    @Test
    void triggerVacancyCheck_IsCalled() {
        rentalService.triggerVacancyCheck(kosId);
    }


    @Test
    void mapToRentalDto_CorrectMapping() {
        String kosName = "Mapped Kos Name";
        RentalDto dto = rentalService.mapToRentalDto(mockRental, kosName);

        assertEquals(mockRental.getId(), dto.getRentalId());
        assertEquals(mockRental.getTenantUserId(), dto.getTenantUserId());
        assertEquals(mockRental.getKosId(), dto.getKosId());
        assertEquals(mockRental.getOwnerUserId(), dto.getOwnerUserId());
        assertEquals(kosName, dto.getKosName());
        assertEquals(mockRental.getSubmittedTenantName(), dto.getSubmittedTenantName());
        assertEquals(mockRental.getSubmittedTenantPhone(), dto.getSubmittedTenantPhone());
        assertEquals(mockRental.getRentalStartDate(), dto.getRentalStartDate());
        assertEquals(mockRental.getRentalDurationMonths(), dto.getRentalDurationMonths());
        assertEquals(mockRental.getRentalEndDate(), dto.getRentalEndDate());
        assertEquals(mockRental.getStatus().toString(), dto.getStatus());
        assertEquals(mockRental.getCreatedAt(), dto.getCreatedAt());
        assertEquals(mockRental.getUpdatedAt(), dto.getUpdatedAt());
    }

    @Test
    void testServiceInteractionException() {
        String message = "Test interaction exception";
        RentalServiceImpl.ServiceInteractionException ex = new RentalServiceImpl.ServiceInteractionException(message);
        assertEquals(message, ex.getMessage());
    }

    @Test
    void testServiceUnavailableException() {
        String message = "Test unavailable exception";
        RentalServiceImpl.ServiceUnavailableException ex = new RentalServiceImpl.ServiceUnavailableException(message);
        assertEquals(message, ex.getMessage());
    }

}