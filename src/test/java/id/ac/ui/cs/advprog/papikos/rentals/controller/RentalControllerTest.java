//package id.ac.ui.cs.advprog.papikos.rentals.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalApplicationRequest;
//import id.ac.ui.cs.advprog.papikos.rentals.dto.RentalDto;
//import id.ac.ui.cs.advprog.papikos.rentals.dto.UpdateRentalSubmissionRequest;
//import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
//import id.ac.ui.cs.advprog.papikos.rentals.exception.ForbiddenException;
//import id.ac.ui.cs.advprog.papikos.rentals.exception.ResourceNotFoundException;
//import id.ac.ui.cs.advprog.papikos.rentals.exception.ValidationException;
//import id.ac.ui.cs.advprog.papikos.rentals.service.RentalService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.hamcrest.Matchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class RentalControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Mock
//    private RentalService rentalService;
//
//    @InjectMocks
//    private RentalController rentalController;
//
//    @Autowired
//    private WebApplicationContext context;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private RentalApplicationRequest validApplicationRequest;
//    private UpdateRentalSubmissionRequest validUpdateRequest;
//    private RentalDto sampleRentalDto;
//    private UUID mockTenantUserId = UUID.randomUUID();
//    private UUID mockOwnerUserId = UUID.randomUUID();
//    private UUID mockRentalId = UUID.randomUUID();
//    private String mockUserRoleTenant = "TENANT";
//    private String mockUserRoleOwner = "OWNER";
//    private UUID validUUID = UUID.randomUUID();
//
//    @BeforeEach
//    void setUp() {
//        // Initialize mocks
//        mockMvc = MockMvcBuilders
//                .webAppContextSetup(context)
//                .apply(springSecurity())
//                .build();
//
//        objectMapper.registerModule(new JavaTimeModule());
//        objectMapper.findAndRegisterModules();
//
//        validApplicationRequest = new RentalApplicationRequest();
//        validApplicationRequest.setKosId(validUUID);
//        validApplicationRequest.setSubmittedTenantName("Test Tenant");
//        validApplicationRequest.setSubmittedTenantPhone("081234567890");
//        validApplicationRequest.setRentalStartDate(LocalDate.now().plusDays(5));
//        validApplicationRequest.setRentalDurationMonths(3);
//
//        validUpdateRequest = new UpdateRentalSubmissionRequest();
//        validUpdateRequest.setSubmittedTenantName("Updated Tenant Name");
//        validUpdateRequest.setRentalDurationMonths(4);
//
//        sampleRentalDto = RentalDto.builder()
//                .rentalId(mockRentalId)
//                .tenantUserId(mockTenantUserId)
//                .kosId(validUUID)
//                .ownerUserId(mockOwnerUserId)
//                .kosName("Cool Kos")
//                .submittedTenantName("Test Tenant")
//                .submittedTenantPhone("081234567890")
//                .rentalStartDate(LocalDate.now().plusDays(5))
//                .rentalDurationMonths(3)
//                .rentalEndDate(LocalDate.now().plusDays(5).plusMonths(3))
//                .status(RentalStatus.PENDING_APPROVAL.toString())
//                .createdAt(LocalDateTime.now().minusMinutes(10))
//                .updatedAt(LocalDateTime.now().minusMinutes(5))
//                .build();
//    }
//
//    // --- POST /rentals (Submit Application) ---
//    @Test
//    @DisplayName("POST /rentals - Success")
//    @WithMockUser(authorities = {"TENANT"})
//    void submitRentalApplication_Success() throws Exception {
//        when(rentalService.submitRentalApplication(eq(mockTenantUserId), any(RentalApplicationRequest.class)))
//                .thenReturn(sampleRentalDto);
//
//        mockMvc.perform(post("/api/v1/rentals")
//                        .with(csrf())
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validApplicationRequest)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status", is(201)))
//                .andExpect(jsonPath("$.message", is("Resource created successfully")))
//                .andExpect(jsonPath("$.data.rentalId", is(mockRentalId)));
//    }
//
//    @Test
//    @DisplayName("POST /rentals - Invalid Request Body")
//    @WithMockUser(authorities = {"TENANT"})
//    void submitRentalApplication_InvalidBody() throws Exception {
//        RentalApplicationRequest invalidRequest = new RentalApplicationRequest(); // Missing required fields
//        mockMvc.perform(post("/api/v1/rentals")
//                        .with(csrf())
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidRequest)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status", is(400)))
//                .andExpect(jsonPath("$.message", containsString("Validation failed")));
//    }
//
//    @Test
//    @DisplayName("POST /rentals - Service throws ResourceNotFoundException")
//    @WithMockUser(authorities = {"TENANT"})
//    void submitRentalApplication_ServiceResourceNotFound() throws Exception {
//        when(rentalService.submitRentalApplication(eq(mockTenantUserId), any(RentalApplicationRequest.class)))
//                .thenThrow(new ResourceNotFoundException("Kos not found"));
//
//        mockMvc.perform(post("/api/v1/rentals")
//                        .with(csrf())
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validApplicationRequest)))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status", is(404)))
//                .andExpect(jsonPath("$.message", is("Kos not found")));
//    }
//
//    // --- GET /rentals/my (Tenant's Rentals) ---
//    @Test
//    @DisplayName("GET /rentals/my - Success")
//    @WithMockUser(authorities = {"TENANT"})
//    void getTenantRentals_Success() throws Exception {
//        when(rentalService.getTenantRentals(eq(mockTenantUserId)))
//                .thenReturn(List.of(sampleRentalDto));
//
//        mockMvc.perform(get("/api/v1/rentals/my")
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.message", is("Success")))
//                .andExpect(jsonPath("$.data", hasSize(1)))
//                .andExpect(jsonPath("$.data[0].rentalId", is(mockRentalId)));
//    }
//
//    @Test
//    @DisplayName("GET /rentals/my - Empty List")
//    @WithMockUser(authorities = {"TENANT"})
//    void getTenantRentals_EmptyList() throws Exception {
//        when(rentalService.getTenantRentals(eq(mockTenantUserId)))
//                .thenReturn(Collections.emptyList());
//
//        mockMvc.perform(get("/api/v1/rentals/my")
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data", hasSize(0)));
//    }
//
//
//    // --- GET /rentals/owner (Owner's Rentals) ---
//    @Test
//    @DisplayName("GET /rentals/owner - Success")
//    @WithMockUser(authorities = {"OWNER"})
//    void getOwnerRentals_Success() throws Exception {
//        when(rentalService.getOwnerRentals(eq(mockOwnerUserId), eq(null), eq(null))) // No status/kosId filter
//                .thenReturn(List.of(sampleRentalDto));
//
//        mockMvc.perform(get("/api/v1/rentals/owner")
//                        .header("X-User-ID", mockOwnerUserId.toString())
//                        .header("X-User-Role", mockUserRoleOwner))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data", hasSize(1)))
//                .andExpect(jsonPath("$.data[0].ownerUserId", is(mockOwnerUserId)));
//    }
//
//    @Test
//    @DisplayName("GET /rentals/owner - With Filters")
//    @WithMockUser(authorities = {"OWNER"})
//    void getOwnerRentals_WithFilters() throws Exception {
//        UUID kosIdFilter = validUUID;
//        RentalStatus statusFilter = RentalStatus.PENDING_APPROVAL;
//        when(rentalService.getOwnerRentals(eq(mockOwnerUserId), eq(statusFilter), eq(kosIdFilter)))
//                .thenReturn(List.of(sampleRentalDto));
//
//        mockMvc.perform(get("/api/v1/rentals/owner")
//                        .header("X-User-ID", mockOwnerUserId.toString())
//                        .header("X-User-Role", mockUserRoleOwner)
//                        .param("status", statusFilter.toString())
//                        .param("kosId", kosIdFilter.toString()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data", hasSize(1)));
//    }
//
//    // --- GET /rentals/{rentalId} ---
//    @Test
//    @DisplayName("GET /rentals/{rentalId} - Success")
//    @WithMockUser // Can be tenant or owner
//    void getRentalById_Success() throws Exception {
//        when(rentalService.getRentalById(eq(mockRentalId), eq(mockTenantUserId))) // Assuming tenant is fetching
//                .thenReturn(sampleRentalDto);
//
//        mockMvc.perform(get("/api/v1/rentals/{rentalId}", mockRentalId)
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data.rentalId", is(mockRentalId)));
//    }
//
//    @Test
//    @DisplayName("GET /rentals/{rentalId} - Not Found")
//    @WithMockUser
//    void getRentalById_NotFound() throws Exception {
//        when(rentalService.getRentalById(eq(mockRentalId), eq(mockTenantUserId)))
//                .thenThrow(new ResourceNotFoundException("Rental not found"));
//
//        mockMvc.perform(get("/api/v1/rentals/{rentalId}", mockRentalId)
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant))
//                .andExpect(status().isNotFound())
//                .andExpect(jsonPath("$.status", is(404)))
//                .andExpect(jsonPath("$.message", is("Rental not found")));
//    }
//
//    @Test
//    @DisplayName("GET /rentals/{rentalId} - Forbidden")
//    @WithMockUser
//    void getRentalById_Forbidden() throws Exception {
//        UUID anotherUserId = UUID.randomUUID();
//        when(rentalService.getRentalById(eq(mockRentalId), eq(anotherUserId)))
//                .thenThrow(new ForbiddenException("User not authorized"));
//
//        mockMvc.perform(get("/api/v1/rentals/{rentalId}", mockRentalId)
//                        .header("X-User-ID", anotherUserId.toString()) // Different user
//                        .header("X-User-Role", mockUserRoleTenant))
//                .andExpect(status().isForbidden())
//                .andExpect(jsonPath("$.status", is(403)))
//                .andExpect(jsonPath("$.message", is("User not authorized")));
//    }
//
//
//    // --- PATCH /rentals/{rentalId} (Tenant Edit Submission) ---
//    @Test
//    @DisplayName("PATCH /rentals/{rentalId} - Tenant Edit Success")
//    @WithMockUser(authorities = {"TENANT"})
//    void editRentalSubmission_Success() throws Exception {
//        RentalDto updatedDto = RentalDto.builder().rentalId(mockRentalId).submittedTenantName("Updated Name").status(RentalStatus.PENDING_APPROVAL.toString()).build();
//        when(rentalService.editRentalSubmission(eq(mockRentalId), eq(mockTenantUserId), any(UpdateRentalSubmissionRequest.class)))
//                .thenReturn(updatedDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}", mockRentalId)
//                        .with(csrf())
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data.submittedTenantName", is("Updated Name")));
//    }
//
//    @Test
//    @DisplayName("PATCH /rentals/{rentalId} - Tenant Edit Not Found")
//    @WithMockUser(authorities = {"TENANT"})
//    void editRentalSubmission_NotFound() throws Exception {
//        when(rentalService.editRentalSubmission(eq(mockRentalId), eq(mockTenantUserId), any(UpdateRentalSubmissionRequest.class)))
//                .thenThrow(new ResourceNotFoundException("Rental not found"));
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}", mockRentalId)
//                        .with(csrf())
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("PATCH /rentals/{rentalId} - Tenant Edit Forbidden (e.g. wrong user or not pending)")
//    @WithMockUser(authorities = {"TENANT"})
//    void editRentalSubmission_Forbidden() throws Exception {
//        when(rentalService.editRentalSubmission(eq(mockRentalId), eq(mockTenantUserId), any(UpdateRentalSubmissionRequest.class)))
//                .thenThrow(new ValidationException("Cannot edit non-pending rental")); // Or ForbiddenException
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}", mockRentalId)
//                        .with(csrf())
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
//                .andExpect(status().isBadRequest()); // Or isForbidden()
//    }
//
//    // --- PATCH /rentals/{rentalId}/cancel (Tenant Cancel) ---
//    @Test
//    @DisplayName("PATCH /rentals/{rentalId}/cancel - Tenant Cancel Success")
//    @WithMockUser(authorities = {"TENANT"})
//    void cancelRental_TenantSuccess() throws Exception {
//        RentalDto cancelledDto = RentalDto.builder().rentalId(mockRentalId).status(RentalStatus.CANCELLED.toString()).build();
//        when(rentalService.cancelRental(eq(mockRentalId), eq(mockTenantUserId), eq(mockUserRoleTenant)))
//                .thenReturn(cancelledDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/cancel", mockRentalId)
//                        .with(csrf())
//                        .header("X-User-ID", mockTenantUserId.toString())
//                        .header("X-User-Role", mockUserRoleTenant))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data.status", is(RentalStatus.CANCELLED.toString())));
//    }
//
//    // --- PATCH /rentals/{rentalId}/approve (Owner Approve) ---
//    @Test
//    @DisplayName("PATCH /rentals/{rentalId}/approve - Owner Approve Success")
//    @WithMockUser(authorities = {"OWNER"})
//    void approveRental_OwnerSuccess() throws Exception {
//        RentalDto approvedDto = RentalDto.builder().rentalId(mockRentalId).status(RentalStatus.APPROVED.toString()).build();
//        when(rentalService.approveRental(eq(mockRentalId), eq(mockOwnerUserId)))
//                .thenReturn(approvedDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/approve", mockRentalId)
//                        .with(csrf())
//                        .header("X-User-ID", mockOwnerUserId.toString())
//                        .header("X-User-Role", mockUserRoleOwner))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data.status", is(RentalStatus.APPROVED.toString())));
//    }
//
//    @Test
//    @DisplayName("PATCH /rentals/{rentalId}/approve - Owner Approve Forbidden (e.g. not owner of kos)")
//    @WithMockUser(authorities = {"OWNER"})
//    void approveRental_OwnerForbidden() throws Exception {
//        when(rentalService.approveRental(eq(mockRentalId), eq(mockOwnerUserId)))
//                .thenThrow(new ForbiddenException("User not authorized to approve"));
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/approve", mockRentalId)
//                        .with(csrf())
//                        .header("X-User-ID", mockOwnerUserId.toString())
//                        .header("X-User-Role", mockUserRoleOwner))
//                .andExpect(status().isForbidden());
//    }
//
//
//    // --- PATCH /rentals/{rentalId}/reject (Owner Reject) ---
//    @Test
//    @DisplayName("PATCH /rentals/{rentalId}/reject - Owner Reject Success")
//    @WithMockUser(authorities = {"OWNER"})
//    void rejectRental_OwnerSuccess() throws Exception {
//        RentalDto rejectedDto = RentalDto.builder().rentalId(mockRentalId).status(RentalStatus.REJECTED.toString()).build();
//        when(rentalService.rejectRental(eq(mockRentalId), eq(mockOwnerUserId)))
//                .thenReturn(rejectedDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/reject", mockRentalId)
//                        .with(csrf())
//                        .header("X-User-ID", mockOwnerUserId.toString())
//                        .header("X-User-Role", mockUserRoleOwner))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status", is(200)))
//                .andExpect(jsonPath("$.data.status", is(RentalStatus.REJECTED.toString())));
//    }
//}