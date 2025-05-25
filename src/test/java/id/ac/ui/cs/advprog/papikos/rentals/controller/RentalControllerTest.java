//package id.ac.ui.cs.advprog.papikos.rentals.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import id.ac.ui.cs.advprog.papikos.rentals.client.KosServiceClient;
//import id.ac.ui.cs.advprog.papikos.rentals.dto.*;
//import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
//import id.ac.ui.cs.advprog.papikos.rentals.response.ApiResponse;
//import id.ac.ui.cs.advprog.papikos.rentals.service.RentalService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.UUID;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class RentalControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private RentalService rentalService;
//
//    @MockBean
//    private Authentication authentication;
//
//    @MockBean
//    private KosServiceClient kosServiceClient;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private UUID userId;
//    private UUID rentalId;
//    private RentalDto rentalDto;
//
//    @BeforeEach
//    void setUp() {
//        userId = UUID.randomUUID();
//        rentalId = UUID.randomUUID();
//        rentalDto = RentalDto.builder().rentalId(rentalId).kosId(UUID.randomUUID()).build();
//    }
//
//    @Test
//    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000", authorities = "TENANT")
//    void testSubmitRentalApplication() throws Exception {
//        RentalApplicationRequest request = new RentalApplicationRequest();
//        when(rentalService.submitRentalApplication(any(), any())).thenReturn(rentalDto);
//
//        mockMvc.perform(post("/api/v1/rentals")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.message").value("Rental application submitted successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000", authorities = "TENANT")
//    void testGetMyRentals() throws Exception {
//        when(rentalService.getTenantRentals(any())).thenReturn(List.of(rentalDto));
//
//        mockMvc.perform(get("/api/v1/rentals/my"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Tenant's rentals fetched successfully"));
//    }
//
//    @Test
//    @WithMockUser(username = "123e4567-e89b-12d3-a456-426614174000", authorities = "OWNER")
//    void testGetOwnerRentals() throws Exception {
//        when(rentalService.getOwnerRentals(any(), any(), any())).thenReturn(List.of(rentalDto));
//
//        mockMvc.perform(get("/api/v1/rentals/owner"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Owner's rentals fetched successfully"));
//    }
//
//    @Test
//    @WithMockUser(authorities = "OWNER")
//    void testApproveRental() throws Exception {
//        when(rentalService.approveRental(any(), any())).thenReturn(rentalDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/approve", rentalId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Rental approved successfully"));
//    }
//
//    @Test
//    @WithMockUser(authorities = "OWNER")
//    void testRejectRental() throws Exception {
//        when(rentalService.rejectRental(any(), any())).thenReturn(rentalDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/reject", rentalId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Rental rejected successfully"));
//    }
//
//    @Test
//    @WithMockUser(authorities = "TENANT")
//    void testEditRentalSubmission() throws Exception {
//        UpdateRentalSubmissionRequest request = new UpdateRentalSubmissionRequest();
//        when(rentalService.editRentalSubmission(any(), any(), any())).thenReturn(rentalDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}", rentalId)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Rental submission updated successfully"));
//    }
//
//    @Test
//    @WithMockUser(authorities = {"OWNER"})
//    void testCancelRentalAsOwner() throws Exception {
//        when(rentalService.cancelRental(any(), any(), eq("OWNER"))).thenReturn(rentalDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/cancel", rentalId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Rental cancelled successfully"));
//    }
//
//    @Test
//    @WithMockUser(authorities = {"TENANT"})
//    void testCancelRentalAsTenant() throws Exception {
//        when(rentalService.cancelRental(any(), any(), eq("TENANT"))).thenReturn(rentalDto);
//
//        mockMvc.perform(patch("/api/v1/rentals/{rentalId}/cancel", rentalId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Rental cancelled successfully"));
//    }
//
//    @Test
//    @WithMockUser(authorities = "OWNER")
//    void testGetRentalById() throws Exception {
//        when(rentalService.getRentalById(any(), any())).thenReturn(rentalDto);
//
//        mockMvc.perform(get("/api/v1/rentals/{rentalId}", rentalId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Rental details fetched successfully"));
//    }
//
//    @Test
//    @WithMockUser(authorities = "OWNER")
//    void testTestEndpoint() throws Exception {
//        UUID kosId = UUID.randomUUID();
//        mockMvc.perform(get("/api/v1/rentals/tes/" + kosId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.message").value("Test successful"));
//    }
//}
