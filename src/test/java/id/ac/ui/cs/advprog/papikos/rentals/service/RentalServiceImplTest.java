//package id.ac.ui.cs.advprog.papikos.rentals.service;
//
//import id.ac.ui.cs.advprog.papikos.rentals.client.*;
//import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalApplicationRequest;
//import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalDto;
//import id.ac.ui.cs.advprog.papikos.rentals.dto.UpdateRentalSubmissionRequest;
//import id.ac.ui.cs.advprog.papikos.rentals.exception.*;
//import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
//import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
//import id.ac.ui.cs.advprog.papikos.rentals.repository.RentalRepository;
//import id.ac.ui.cs.advprog.papikos.rentals.service.RentalServiceImpl;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class RentalServiceImplTest {
//
//    @Mock
//    private RentalRepository rentalRepository;
//    @Mock
//    private KosServiceClient kosServiceClient;
//    @Mock
//    private AuthServiceClient authServiceClient;
//    @Mock
//    private NotificationServiceClient notificationServiceClient;
//
//    @InjectMocks
//    private RentalServiceImpl rentalService;
//
//    private RentalApplicationRequest applicationRequest;
//    private UpdateRentalSubmissionRequest updateRequest;
//    private UUID tenantUserId;
//    private UUID KosId;
//    private UUID ownerUserId;
//    private UUID rentalId;
//    private Rental sampleRental;
//    private KosDetailsDto mockKosDetails;
//
//    @BeforeEach
//    void setUp() {
//        tenantUserId = UUID.randomUUID();
//        KosId = UUID.randomUUID();
//        ownerUserId = UUID.randomUUID();
//        rentalId = UUID.randomUUID();
//
//        applicationRequest = new RentalApplicationRequest();
//        applicationRequest.setKosId(KosId);
//        applicationRequest.setSubmittedTenantName("Test Tenant");
//        applicationRequest.setSubmittedTenantPhone("08123456789");
//        applicationRequest.setRentalStartDate(LocalDate.now().plusDays(7));
//        applicationRequest.setRentalDurationMonths(6);
//
//        updateRequest = new UpdateRentalSubmissionRequest();
//        updateRequest.setSubmittedTenantName("Updated Name");
//        updateRequest.setRentalDurationMonths(7);
//
//        mockKosDetails = new KosDetailsDto(KosId, ownerUserId, "Test Kos", 5, true);
//
//        sampleRental = new Rental();
//        sampleRental.setId(rentalId);
//        sampleRental.setTenantUserId(tenantUserId);
//        sampleRental.setKosId(KosId);
//        sampleRental.setOwnerUserId(ownerUserId);
//        sampleRental.setStatus(RentalStatus.PENDING_APPROVAL);
//        sampleRental.setSubmittedTenantName(applicationRequest.getSubmittedTenantName());
//        sampleRental.setSubmittedTenantPhone(applicationRequest.getSubmittedTenantPhone());
//        sampleRental.setRentalStartDate(applicationRequest.getRentalStartDate());
//        sampleRental.setRentalDurationMonths(applicationRequest.getRentalDurationMonths());
//        sampleRental.setRentalEndDate(applicationRequest.getRentalStartDate().plusMonths(applicationRequest.getRentalDurationMonths()));
//    }
//
//    @Test
//    void submitRentalApplication_Success() {
//        when(kosServiceClient.getKosDetails(KosId)).thenReturn(mockKosDetails);
//        when(kosServiceClient.getActiveRentalsCountForKos(KosId)).thenReturn(0L);
//        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> {
//            Rental r = invocation.getArgument(0);
//            if (r.getId() == null) r.setId(UUID.randomUUID());
//            return r;
//        });
//        doNothing().when(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//
//        RentalDto result = rentalService.submitRentalApplication(tenantUserId, applicationRequest);
//
//        assertNotNull(result);
//        assertNotNull(result.getRentalId());
//        assertEquals(KosId, result.getKosId());
//        assertEquals(tenantUserId, result.getTenantUserId());
//        assertEquals(RentalStatus.PENDING_APPROVAL.toString(), result.getStatus());
//        verify(rentalRepository).save(any(Rental.class));
//        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//    }
//
//    @Test
//    void submitRentalApplication_KosNotFound() {
//        when(kosServiceClient.getKosDetails(KosId)).thenReturn(null);
//        assertThrows(ResourceNotFoundException.class, () -> rentalService.submitRentalApplication(tenantUserId, applicationRequest));
//        verify(rentalRepository, never()).save(any());
//    }
//
//    @Test
//    void submitRentalApplication_KosNotListed() {
//        KosDetailsDto unlistedKos = new KosDetailsDto(KosId, ownerUserId, "Test Kos", 5, false);
//        when(kosServiceClient.getKosDetails(KosId)).thenReturn(unlistedKos);
//        assertThrows(ValidationException.class, () -> rentalService.submitRentalApplication(tenantUserId, applicationRequest));
//    }
//
//    @Test
//    void submitRentalApplication_NoVacancy() {
//        when(kosServiceClient.getKosDetails(KosId)).thenReturn(mockKosDetails);
//        when(kosServiceClient.getActiveRentalsCountForKos(KosId)).thenReturn(5L); // Max rooms = 5
//        assertThrows(ValidationException.class, () -> rentalService.submitRentalApplication(tenantUserId, applicationRequest));
//    }
//
//    // --- Get Tenant Rentals ---
//    @Test
//    void getTenantRentals_Success() {
//        when(rentalRepository.findByTenantUserId(tenantUserId)).thenReturn(List.of(sampleRental));
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//
//        List<RentalDto> results = rentalService.getTenantRentals(tenantUserId);
//
//        assertFalse(results.isEmpty());
//        assertEquals(1, results.size());
//        assertEquals(sampleRental.getId(), results.get(0).getRentalId());
//    }
//
//    // --- Get Owner Rentals ---
//    @Test
//    void getOwnerRentals_Success() {
//        when(rentalRepository.findByOwnerUserId(ownerUserId)).thenReturn(List.of(sampleRental));
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//
//        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, null, null);
//        assertFalse(results.isEmpty());
//        assertEquals(sampleRental.getId(), results.get(0).getRentalId());
//    }
//
//    @Test
//    void getOwnerRentals_Filtered_Success() {
//        Rental rental2 = new Rental(); // Another rental for different Kos or status
//        // ... setup rental2 ...
//
//        // This test needs more specific mocking for the filtering logic inside getOwnerRentals
//        // For simplicity, assuming the repository call itself is what we'd refine or that findAll + stream filter is tested
//        when(rentalRepository.findAll()).thenReturn(List.of(sampleRental)); // Mock a broader set if filtering in service
//        when(kosServiceClient.getKosDetails(KosId)).thenReturn(mockKosDetails);
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//
//
//        List<RentalDto> results = rentalService.getOwnerRentals(ownerUserId, RentalStatus.PENDING_APPROVAL, KosId);
//        assertFalse(results.isEmpty());
//        assertEquals(sampleRental.getId(), results.get(0).getRentalId());
//    }
//
//
//    // --- Get Rental By ID ---
//    @Test
//    void getRentalById_TenantSuccess() {
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//
//        RentalDto result = rentalService.getRentalById(rentalId, tenantUserId); // tenantUserId is the requester
//        assertEquals(rentalId, result.getRentalId());
//    }
//
//    @Test
//    void getRentalById_OwnerSuccess() {
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//
//        RentalDto result = rentalService.getRentalById(rentalId, ownerUserId); // ownerUserId is the requester
//        assertEquals(rentalId, result.getRentalId());
//    }
//
//    @Test
//    void getRentalById_NotFound() {
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.empty());
//        assertThrows(ResourceNotFoundException.class, () -> rentalService.getRentalById(rentalId, tenantUserId));
//    }
//
//    @Test
//    void getRentalById_Forbidden() {
//        UUID otherUserId = UUID.randomUUID();
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//        // No need to mock kosServiceClient as ForbiddenException should be thrown before
//        assertThrows(ForbiddenException.class, () -> rentalService.getRentalById(rentalId, otherUserId));
//    }
//
//    // --- Edit Rental Submission ---
//    @Test
//    void editRentalSubmission_Success() {
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental)); // sampleRental has PENDING_APPROVAL status
//        when(rentalRepository.save(any(Rental.class))).thenReturn(sampleRental); // Return the same object for simplicity
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//
//
//        RentalDto result = rentalService.editRentalSubmission(rentalId, tenantUserId, updateRequest);
//        assertEquals("Updated Name", result.getSubmittedTenantName()); // Verify one of the changes
//        verify(rentalRepository).save(sampleRental);
//    }
//
//    @Test
//    void editRentalSubmission_NotPendingApproval() {
//        sampleRental.setStatus(RentalStatus.APPROVED);
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//        assertThrows(ValidationException.class, () -> rentalService.editRentalSubmission(rentalId, tenantUserId, updateRequest));
//    }
//
//    @Test
//    void editRentalSubmission_Forbidden() {
//        UUID wrongTenantId = UUID.randomUUID();
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//        assertThrows(ForbiddenException.class, () -> rentalService.editRentalSubmission(rentalId, wrongTenantId, updateRequest));
//    }
//
//    // --- Cancel Rental ---
//    @Test
//    void cancelRental_TenantSuccess() {
//        sampleRental.setStatus(RentalStatus.APPROVED); // Can cancel an approved rental
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//        doNothing().when(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//
//
//        RentalDto result = rentalService.cancelRental(rentalId, tenantUserId, "TENANT");
//        assertEquals(RentalStatus.CANCELLED.toString(), result.getStatus());
//        verify(rentalRepository).save(argThat(r -> r.getStatus() == RentalStatus.CANCELLED));
//        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//    }
//
//    @Test
//    void cancelRental_AlreadyCancelled() {
//        sampleRental.setStatus(RentalStatus.CANCELLED);
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//
//        assertThrows(ValidationException.class, () -> rentalService.cancelRental(rentalId, tenantUserId, "TENANT"));
//    }
//
//
//    // --- Approve Rental ---
//    @Test
//    void approveRental_OwnerSuccess() {
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental)); // PENDING_APPROVAL
//        when(kosServiceClient.getKosDetails(KosId)).thenReturn(mockKosDetails);
//        when(kosServiceClient.getActiveRentalsCountForKos(KosId)).thenReturn(0L); // Vacancy
//        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//        doNothing().when(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//
//        RentalDto result = rentalService.approveRental(rentalId, ownerUserId);
//        assertEquals(RentalStatus.APPROVED.toString(), result.getStatus());
//        verify(rentalRepository).save(argThat(r -> r.getStatus() == RentalStatus.APPROVED));
//        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//    }
//
//    @Test
//    void approveRental_NoVacancy() {
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental));
//        when(kosServiceClient.getKosDetails(KosId)).thenReturn(mockKosDetails);
//        when(kosServiceClient.getActiveRentalsCountForKos(KosId)).thenReturn((long) mockKosDetails.getTotalRooms()); // No vacancy
//
//        assertThrows(ValidationException.class, () -> rentalService.approveRental(rentalId, ownerUserId));
//        verify(rentalRepository, never()).save(any());
//    }
//
//
//    // --- Reject Rental ---
//    @Test
//    void rejectRental_OwnerSuccess() {
//        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(sampleRental)); // PENDING_APPROVAL
//        when(rentalRepository.save(any(Rental.class))).thenAnswer(inv -> inv.getArgument(0));
//        when(kosServiceClient.getKosDetails(sampleRental.getKosId())).thenReturn(mockKosDetails);
//        doNothing().when(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//
//        RentalDto result = rentalService.rejectRental(rentalId, ownerUserId);
//        assertEquals(RentalStatus.REJECTED.toString(), result.getStatus());
//        verify(rentalRepository).save(argThat(r -> r.getStatus() == RentalStatus.REJECTED));
//        verify(notificationServiceClient).sendNotification(any(NotificationRequest.class));
//    }
//}