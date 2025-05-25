package id.ac.ui.cs.advprog.papikos.rentals.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import id.ac.ui.cs.advprog.papikos.rentals.client.*;
import id.ac.ui.cs.advprog.papikos.rentals.config.RabbitMQConfig;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate; // Not directly used but part of class

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
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
    private RestTemplate restTemplate; // Though not used directly in the methods being tested here
    @Mock
    private ObjectMapper objectMapper; // Though not used directly in the methods being tested here

    @InjectMocks
    private RentalServiceImpl rentalService;

    private UUID kosId;
    private UUID tenantUserId;
    private UUID ownerUserId;
    private UUID rentalId;
    private KosDetailsDto kosDetailsDto;
    private Rental rental;
    private RentalApplicationRequest rentalApplicationRequest;

    @BeforeEach
    void setUp() {
        kosId = UUID.randomUUID();
        tenantUserId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        rentalId = UUID.randomUUID();

        kosDetailsDto = new KosDetailsDto(kosId, ownerUserId, "Kos Test", "Address", "Desc", 5, true, BigDecimal.valueOf(1000000), new Date(), new Date());
        rental = new Rental(rentalId, tenantUserId, kosId, ownerUserId, "Tenant Name", "08123", LocalDate.now(), 1, LocalDate.now().plusMonths(1), RentalStatus.PENDING_APPROVAL, null, null);
        rentalApplicationRequest = new RentalApplicationRequest();
        rentalApplicationRequest.setKosId(kosId);
        rentalApplicationRequest.setSubmittedTenantName("Tenant Name");
        rentalApplicationRequest.setSubmittedTenantPhone("08123");
        rentalApplicationRequest.setRentalStartDate(LocalDate.now());
        rentalApplicationRequest.setRentalDurationMonths(1);
    }

    private KosApiResponseWrapper<KosDetailsDto> mockKosApiResponse(KosDetailsDto data, HttpStatus status, String message) {
        KosApiResponseWrapper<KosDetailsDto> wrapper = new KosApiResponseWrapper<>();
        wrapper.setData(data);
        wrapper.setStatus(status.value());
        wrapper.setMessage(message);
        return wrapper;
    }

    // Helper method for FeignException.NotFound
    private FeignException.NotFound createFeignNotFoundException(String message) {
        Request mockRequest = Request.create(Request.HttpMethod.GET, "/dummy", Collections.emptyMap(), null, Charset.defaultCharset(), null);
        return new FeignException.NotFound(message, mockRequest, null, Collections.emptyMap());
    }

    // Helper method for generic FeignException
    private FeignException createGenericFeignException(int status, String message) {
        Request mockRequest = Request.create(Request.HttpMethod.GET, "/dummy", Collections.emptyMap(), null, Charset.defaultCharset(), null);
        byte[] body = message.getBytes(Charset.defaultCharset());
        return FeignException.errorStatus("methodKey", Response.builder()
                .status(status)
                .reason("Error")
                .request(mockRequest)
                .headers(Collections.emptyMap())
                .body(body)
                .build());
    }

    @Test
    void tryFetchKosDetail_success() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        assertDoesNotThrow(() -> rentalService.tryFetchKosDetail(kosId));
        verify(kosServiceClient).getKosDetailsApiResponse(kosId);
    }

    @Test
    void tryFetchKosDetail_failure_serviceUnavailable() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(createGenericFeignException(500, "Internal Server Error"));
        assertThrows(RentalServiceImpl.ServiceUnavailableException.class, () -> rentalService.tryFetchKosDetail(kosId));
    }

    @Test
    void fetchKosDetails_success() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        KosDetailsDto result = rentalService.fetchKosDetails(kosId); // Accessing private method via reflection for test is an option, or test through public methods
        // Test via public method for now.
        assertNotNull(result);
        assertEquals(kosDetailsDto.getName(), result.getName());
    }

    @Test
    void fetchKosDetails_feignNotFound() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(createFeignNotFoundException("Kos not found"));
        assertThrows(ResourceNotFoundException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_feignOtherError() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(createGenericFeignException(503, "Service Unavailable"));
        assertThrows(RentalServiceImpl.ServiceUnavailableException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_nullResponseWrapper() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(null);
        assertThrows(RentalServiceImpl.ServiceUnavailableException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_nonOkStatusInWrapper_notFound() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(null, HttpStatus.NOT_FOUND, "Not Found from Service"));
        assertThrows(ResourceNotFoundException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_nonOkStatusInWrapper_otherError() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(null, HttpStatus.BAD_REQUEST, "Bad Request from Service"));
        assertThrows(RentalServiceImpl.ServiceInteractionException.class, () -> rentalService.fetchKosDetails(kosId));
    }

    @Test
    void fetchKosDetails_okStatusButNullData() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(null, HttpStatus.OK, "Success but no data"));
        assertThrows(ResourceNotFoundException.class, () -> rentalService.fetchKosDetails(kosId));
    }


    @Test
    void submitRentalApplication_success() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        RentalDto result = rentalService.submitRentalApplication(tenantUserId, rentalApplicationRequest);

        assertNotNull(result);
        assertEquals(rental.getId(), result.getRentalId());
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.TOPIC_EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_RENTAL_CREATED), any(RentalEvent.class));
    }

    @Test
    void submitRentalApplication_kosNotListed() {
        kosDetailsDto.setListed(false);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        assertThrows(ValidationException.class, () -> rentalService.submitRentalApplication(tenantUserId, rentalApplicationRequest));
    }

    @Test
    void submitRentalApplication_noRoomsAvailable() {
        kosDetailsDto.setNumRooms(1); // Set numRooms to 1
        List<Rental> activeRentals = Collections.singletonList(new Rental()); // Simulate one active rental
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(activeRentals);

        assertThrows(ValidationException.class, () -> rentalService.submitRentalApplication(tenantUserId, rentalApplicationRequest));
    }

    @Test
    void submitRentalApplication_rabbitMqError() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        doThrow(new RuntimeException("RabbitMQ error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class));

        RentalDto result = rentalService.submitRentalApplication(tenantUserId, rentalApplicationRequest);
        // Should still succeed, but log an error for RabbitMQ
        assertNotNull(result);
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class)); // Verify notification still sent
    }


    @Test
    void getTenantRentals_success() {
        when(rentalRepository.findByTenantUserId(tenantUserId)).thenReturn(Collections.singletonList(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success")); // For getKosName

        List<RentalDto> results = rentalService.getTenantRentals(tenantUserId);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(kosDetailsDto.getName(), results.get(0).getKosName());
    }

    @Test
    void getOwnerRentals_success_noFilter() {
        when(rentalRepository.findByOwnerUserId(ownerUserId)).thenReturn(Collections.singletonList(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success")); // For getKosName

        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, null, null);
        assertFalse(results.isEmpty());
    }

    @Test
    void getOwnerRentals_success_withKosIdFilter_owned() {
        List<Rental> allRentals = new ArrayList<>();
        Rental rentalForKos1 = new Rental(UUID.randomUUID(), tenantUserId, kosId, ownerUserId, "Name1", "Phone1", LocalDate.now(), 1, LocalDate.now().plusMonths(1), RentalStatus.PENDING_APPROVAL, null, null);
        UUID otherKosId = UUID.randomUUID();
        KosDetailsDto otherKosDetails = new KosDetailsDto(otherKosId, ownerUserId, "Other Kos", "Addr", "Desc", 2, true, BigDecimal.ONE, new Date(), new Date());
        Rental rentalForKos2 = new Rental(UUID.randomUUID(), tenantUserId, otherKosId, ownerUserId, "Name2", "Phone2", LocalDate.now(), 1, LocalDate.now().plusMonths(1), RentalStatus.PENDING_APPROVAL, null, null);
        allRentals.add(rentalForKos1);
        allRentals.add(rentalForKos2);

        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        when(rentalRepository.findAll()).thenReturn(allRentals);


        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, null, kosId);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals(kosId, results.get(0).getKosId());
        assertEquals(kosDetailsDto.getName(), results.get(0).getKosName());
    }

    @Test
    void getOwnerRentals_withKosIdFilter_notOwned() {
        UUID otherOwnerId = UUID.randomUUID();
        KosDetailsDto notOwnedKosDetails = new KosDetailsDto(kosId, otherOwnerId, "Not My Kos", "Addr", "Desc", 1, true, BigDecimal.TEN, new Date(), new Date());
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(notOwnedKosDetails, HttpStatus.OK, "Success"));

        assertThrows(ForbiddenException.class, () -> rentalService.getOwnerRentals(ownerUserId, null, kosId));
    }

    @Test
    void getOwnerRentals_withStatusFilter() {
        rental.setStatus(RentalStatus.APPROVED); // Change status for filtering
        when(rentalRepository.findByOwnerUserId(ownerUserId)).thenReturn(Collections.singletonList(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));


        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, RentalStatus.APPROVED, null);
        assertFalse(results.isEmpty());
        assertEquals(RentalStatus.APPROVED.name(), results.get(0).getStatus());
    }


    @Test
    void getRentalById_success_tenant() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        RentalDto result = rentalService.getRentalById(rentalId, tenantUserId);
        assertNotNull(result);
    }

    @Test
    void getRentalById_success_owner() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        RentalDto result = rentalService.getRentalById(rentalId, ownerUserId);
        assertNotNull(result);
    }

    @Test
    void getRentalById_notFound() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> rentalService.getRentalById(rentalId, tenantUserId));
    }

    @Test
    void getRentalById_forbidden() {
        UUID otherUserId = UUID.randomUUID();
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        // No need to mock kosServiceClient here as it's not reached
        assertThrows(ForbiddenException.class, () -> rentalService.getRentalById(rentalId, otherUserId));
    }

    @Test
    void editRentalSubmission_success() {
        UpdateRentalSubmissionRequest updateRequest = new UpdateRentalSubmissionRequest();
        updateRequest.setSubmittedTenantName("New Name");
        updateRequest.setRentalDurationMonths(3);

        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental); // Simulate save
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));

        RentalDto result = rentalService.editRentalSubmission(rentalId, tenantUserId, updateRequest);

        assertNotNull(result);
        assertEquals("New Name", rental.getSubmittedTenantName()); // Check if entity was updated
        assertEquals(3, rental.getRentalDurationMonths());
        verify(rentalRepository).save(rental);
    }

    @Test
    void editRentalSubmission_rentalNotFound() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());
        UpdateRentalSubmissionRequest updateRequest = new UpdateRentalSubmissionRequest();
        assertThrows(ResourceNotFoundException.class, () -> rentalService.editRentalSubmission(rentalId, tenantUserId, updateRequest));
    }

    @Test
    void editRentalSubmission_forbiddenUser() {
        UUID otherTenantId = UUID.randomUUID();
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        UpdateRentalSubmissionRequest updateRequest = new UpdateRentalSubmissionRequest();
        assertThrows(ForbiddenException.class, () -> rentalService.editRentalSubmission(rentalId, otherTenantId, updateRequest));
    }

    @Test
    void editRentalSubmission_invalidStatus() {
        rental.setStatus(RentalStatus.APPROVED);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        UpdateRentalSubmissionRequest updateRequest = new UpdateRentalSubmissionRequest();
        assertThrows(ValidationException.class, () -> rentalService.editRentalSubmission(rentalId, tenantUserId, updateRequest));
    }

    @Test
    void editRentalSubmission_partialUpdate() {
        UpdateRentalSubmissionRequest updateRequest = new UpdateRentalSubmissionRequest();
        updateRequest.setSubmittedTenantPhone("0000"); // Only update phone

        String originalName = rental.getSubmittedTenantName();
        LocalDate originalDate = rental.getRentalStartDate();
        Integer originalDuration = rental.getRentalDurationMonths();


        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(i -> i.getArgument(0)); // return the modified rental
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));


        RentalDto result = rentalService.editRentalSubmission(rentalId, tenantUserId, updateRequest);

        assertNotNull(result);
        assertEquals("0000", rental.getSubmittedTenantPhone());
        assertEquals(originalName, rental.getSubmittedTenantName()); // Name should remain unchanged
        assertEquals(originalDate, rental.getRentalStartDate());
        assertEquals(originalDuration, rental.getRentalDurationMonths());
        verify(rentalRepository).save(rental);
    }


    @Test
    void cancelRental_successByTenant_pending() {
        rental.setStatus(RentalStatus.PENDING_APPROVAL);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));

        RentalDto result = rentalService.cancelRental(rentalId, tenantUserId, "TENANT");
        assertEquals(RentalStatus.CANCELLED.name(), result.getStatus());
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void cancelRental_successByTenant_approved() {
        rental.setStatus(RentalStatus.APPROVED);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));

        RentalDto result = rentalService.cancelRental(rentalId, tenantUserId, "TENANT");
        assertEquals(RentalStatus.CANCELLED.name(), result.getStatus());
    }

    @Test
    void cancelRental_forbiddenUser_notTenant() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        assertThrows(ForbiddenException.class, () -> rentalService.cancelRental(rentalId, ownerUserId, "OWNER")); // Owner trying tenant flow
    }

    @Test
    void cancelRental_forbiddenUser_wrongTenant() {
        UUID wrongTenantId = UUID.randomUUID();
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        assertThrows(ForbiddenException.class, () -> rentalService.cancelRental(rentalId, wrongTenantId, "TENANT"));
    }


    @Test
    void cancelRental_invalidStatus() {
        rental.setStatus(RentalStatus.ACTIVE);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        assertThrows(ValidationException.class, () -> rentalService.cancelRental(rentalId, tenantUserId, "TENANT"));
    }

    @Test
    void approveRental_success() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);

        RentalDto result = rentalService.approveRental(rentalId, ownerUserId);

        assertEquals(RentalStatus.APPROVED.name(), result.getStatus());
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.TOPIC_EXCHANGE_NAME), eq(RabbitMQConfig.ROUTING_KEY_RENTAL_APPROVED), any(RentalEvent.class));
    }

    @Test
    void approveRental_noRoomsAvailable_concurrentApproval() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success")); // kosDetailsDto has 5 rooms

        List<Rental> activeRentals = new ArrayList<>();
        for (int i = 0; i < kosDetailsDto.getNumRooms(); i++) {
            activeRentals.add(new Rental()); // Add dummy rentals
        }
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(activeRentals);

        assertThrows(ValidationException.class, () -> rentalService.approveRental(rentalId, ownerUserId));
        verify(rentalRepository, never()).save(any(Rental.class)); // Ensure rental is not saved
    }


    @Test
    void approveRental_forbiddenUser() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        assertThrows(ForbiddenException.class, () -> rentalService.approveRental(rentalId, tenantUserId)); // Tenant trying to approve
    }

    @Test
    void approveRental_invalidStatus() {
        rental.setStatus(RentalStatus.APPROVED);
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        assertThrows(ValidationException.class, () -> rentalService.approveRental(rentalId, ownerUserId));
    }

    @Test
    void approveRental_rabbitMqError() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        when(rentalRepository.findByKosIdAndStatusIn(eq(kosId), anyList())).thenReturn(Collections.emptyList());
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        doThrow(new RuntimeException("RabbitMQ error")).when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(RentalEvent.class));

        RentalDto result = rentalService.approveRental(rentalId, ownerUserId);
        // Should still succeed, but log an error for RabbitMQ
        assertNotNull(result);
        assertEquals(RentalStatus.APPROVED.name(), result.getStatus());
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }


    @Test
    void rejectRental_success() {
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental);
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success")); // For getKosName in notification

        RentalDto result = rentalService.rejectRental(rentalId, ownerUserId);
        assertEquals(RentalStatus.REJECTED.name(), result.getStatus());
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void getKosName_success() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenReturn(mockKosApiResponse(kosDetailsDto, HttpStatus.OK, "Success"));
        String name = rentalService.getKosName(kosId);
        assertEquals(kosDetailsDto.getName(), name);
    }

    @Test
    void getKosName_resourceNotFound() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(createFeignNotFoundException("Not found"));
        String name = rentalService.getKosName(kosId);
        assertTrue(name.startsWith("Kos (ID: "));
    }

    @Test
    void getKosName_serviceUnavailable() {
        when(kosServiceClient.getKosDetailsApiResponse(kosId)).thenThrow(createGenericFeignException(503, "Unavailable"));
        String name = rentalService.getKosName(kosId);
        assertTrue(name.startsWith("Kos (ID: "));
    }

//    @Test
//    void sendNotification_success() {
//        doNothing().when(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//        assertDoesNotThrow(() -> rentalService.sendNotification(tenantUserId, "TYPE", "Title", "Msg", rentalId)); // Private method
//        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//    }

    @Test
    void sendNotification_failure() {
        doThrow(new RuntimeException("Notification service error")).when(notificationServiceClient).sendNotification(any(NotificationRequest.class));
        assertDoesNotThrow(() -> rentalService.sendNotification(tenantUserId, "TYPE", "Title", "Msg", rentalId));
        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class)); // Ensure it was still called
    }

    @Test
    void triggerVacancyCheck_logsInfo() {
        rentalService.triggerVacancyCheck(kosId);
    }

    @Test
    void mapToRentalDto_mapsCorrectly() {
        Rental rentalEntity = new Rental();
        rentalEntity.setId(rentalId);
        rentalEntity.setTenantUserId(tenantUserId);
        rentalEntity.setKosId(kosId);
        rentalEntity.setOwnerUserId(ownerUserId);
        rentalEntity.setSubmittedTenantName("Test Tenant");
        rentalEntity.setSubmittedTenantPhone("12345");
        rentalEntity.setRentalStartDate(LocalDate.of(2024,1,1));
        rentalEntity.setRentalDurationMonths(6);
        rentalEntity.setRentalEndDate(LocalDate.of(2024,7,1));
        rentalEntity.setStatus(RentalStatus.APPROVED);

        String kosName = "Beautiful Kos";

        RentalDto dto = rentalService.mapToRentalDto(rentalEntity, kosName);

        assertEquals(rentalId, dto.getRentalId());
        assertEquals(tenantUserId, dto.getTenantUserId());
        assertEquals(kosId, dto.getKosId());
        assertEquals(ownerUserId, dto.getOwnerUserId());
        assertEquals(kosName, dto.getKosName());
        assertEquals("Test Tenant", dto.getSubmittedTenantName());
        assertEquals("12345", dto.getSubmittedTenantPhone());
        assertEquals(LocalDate.of(2024,1,1), dto.getRentalStartDate());
        assertEquals(6, dto.getRentalDurationMonths());
        assertEquals(LocalDate.of(2024,7,1), dto.getRentalEndDate());
        assertEquals(RentalStatus.APPROVED.name(), dto.getStatus());
    }

    @Test
    void testServiceInteractionException() {
        RentalServiceImpl.ServiceInteractionException ex = new RentalServiceImpl.ServiceInteractionException("Test");
        assertEquals("Test", ex.getMessage());
    }

    @Test
    void testServiceUnavailableException() {
        RentalServiceImpl.ServiceUnavailableException ex = new RentalServiceImpl.ServiceUnavailableException("Test");
        assertEquals("Test", ex.getMessage());
    }

}