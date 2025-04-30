package id.ac.ui.cs.advprog.papikos.rentals.service;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.model.KosDummy;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import id.ac.ui.cs.advprog.papikos.rentals.model.TenantDummy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import id.ac.ui.cs.advprog.papikos.rentals.repository.RentalRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RentalServiceImplTest {

    @Mock
    RentalRepository rentalRepository;

    @InjectMocks
    RentalServiceImpl rentalService;

    Rental rental1;
    Rental rentalForUpdate;
    KosDummy dummyKos;
    TenantDummy dummyTenant;
    String rental1Id;

    @BeforeEach
    void setUp() {
        // Data dummy untuk tes
        dummyKos = new KosDummy();
        dummyKos.setId(UUID.randomUUID().toString());
        dummyTenant = new TenantDummy();
        dummyTenant.setId(UUID.randomUUID().toString());

        rental1Id = UUID.randomUUID().toString();
        rental1 = new Rental(rental1Id, dummyKos, dummyTenant, "Penyewa Satu", "081111", "2024-01-01", 3);

        // Data untuk update (bisa objek baru atau modifikasi yg lama)
        rentalForUpdate = new Rental(rental1Id, dummyKos, dummyTenant, "Penyewa Satu Update", "081111-NEW", "2024-01-01", 4, RentalStatus.CONFIRMED.name());
    }

    @Test
    void testCreateRental_whenIdNotExists_shouldSaveRental() {
        when(rentalRepository.findById(rental1.getId())).thenReturn(Optional.empty());
        when(rentalRepository.save(any(Rental.class))).thenReturn(rental1); // Gunakan any() jika objeknya dibuat di dalam service

        Rental createdRental = rentalService.createRental(rental1);

        assertNotNull(createdRental);
        assertEquals(rental1.getId(), createdRental.getId());
        assertEquals(RentalStatus.PENDING, createdRental.getStatusEnum()); // Cek status default

        verify(rentalRepository, times(1)).findById(rental1.getId());
        verify(rentalRepository, times(1)).save(rental1); // atau any(Rental.class)
    }

    @Test
    void testCreateRental_whenIdExists_shouldThrowIllegalArgumentException() {
        when(rentalRepository.findById(rental1.getId())).thenReturn(Optional.of(rental1));

        assertThrows(IllegalArgumentException.class, () -> {
            rentalService.createRental(rental1);
        }, "Should throw IllegalArgumentException when rental ID already exists");

        verify(rentalRepository, times(1)).findById(rental1.getId());
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void testUpdateRental_whenIdExists_shouldUpdateAndSaveRental() {
        when(rentalRepository.findById(rental1Id)).thenReturn(Optional.of(rental1));
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mengembalikan argumen yang diterima save

        Rental updatedRental = rentalService.updateRental(rental1Id, rentalForUpdate); // rentalForUpdate berisi data baru

        assertNotNull(updatedRental);
        assertEquals(rental1Id, updatedRental.getId());
        assertEquals("Penyewa Satu Update", updatedRental.getFullName()); // Cek data baru
        assertEquals(4, updatedRental.getDurationMonths()); // Cek data baru
        assertEquals(RentalStatus.CONFIRMED, updatedRental.getStatusEnum()); // Cek data baru

        verify(rentalRepository, times(1)).findById(rental1Id);
        verify(rentalRepository, times(1)).save(argThat(savedRental ->
                savedRental.getId().equals(rental1Id) &&
                        savedRental.getFullName().equals("Penyewa Satu Update") &&
                        savedRental.getDurationMonths() == 4 &&
                        savedRental.getStatusEnum() == RentalStatus.CONFIRMED
        ));
    }

    @Test
    void testUpdateRental_whenIdNotExists_shouldThrowNoSuchElementException() {
        when(rentalRepository.findById(rental1Id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            rentalService.updateRental(rental1Id, rentalForUpdate);
        }, "Should throw NoSuchElementException when rental ID not found for update");

        verify(rentalRepository, times(1)).findById(rental1Id);
        verify(rentalRepository, never()).save(any(Rental.class));
    }

    @Test
    void testFindRentalById_whenIdExists_shouldReturnRental() {
        when(rentalRepository.findById(rental1Id)).thenReturn(Optional.of(rental1));

        Rental foundRental = rentalService.findRentalById(rental1Id);

        assertNotNull(foundRental);
        assertEquals(rental1Id, foundRental.getId());

        verify(rentalRepository, times(1)).findById(rental1Id);
    }

    @Test
    void testFindRentalById_whenIdNotExists_shouldThrowNoSuchElementException() {
        when(rentalRepository.findById(rental1Id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            rentalService.findRentalById(rental1Id);
        }, "Should throw NoSuchElementException when rental ID not found");

        verify(rentalRepository, times(1)).findById(rental1Id);
    }

    @Test
    void testFindAllRentals_shouldReturnListOfRentals() {
        List<Rental> rentalList = Arrays.asList(rental1, new Rental(UUID.randomUUID().toString(), dummyKos, dummyTenant, "Penyewa Dua", "082222", "2024-02-01", 6));
        when(rentalRepository.findAll()).thenReturn(rentalList);

        List<Rental> foundRentals = rentalService.findAllRentals();

        assertNotNull(foundRentals);
        assertEquals(2, foundRentals.size());
        assertEquals(rentalList, foundRentals); // Atau cek elemen individual

        verify(rentalRepository, times(1)).findAll();
    }

    @Test
    void testFindAllRentals_whenNoRentals_shouldReturnEmptyList() {
        when(rentalRepository.findAll()).thenReturn(Collections.emptyList());

        List<Rental> foundRentals = rentalService.findAllRentals();

        assertNotNull(foundRentals);
        assertTrue(foundRentals.isEmpty());

        verify(rentalRepository, times(1)).findAll();
    }

     @Test
     void testCancelRental_whenIdExists_shouldUpdateStatusToCancelledAndSave() {
         // Arrange: Atur mock findById untuk menemukan rental
         when(rentalRepository.findById(rental1Id)).thenReturn(Optional.of(rental1));
         // Arrange: Atur mock save
         when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

         // Act: Panggil service method
         rentalService.cancelRental(rental1Id);

         // Verify: Pastikan findById dipanggil sekali
         verify(rentalRepository, times(1)).findById(rental1Id);
         // Verify: Pastikan save dipanggil sekali dengan status CANCELLED
         verify(rentalRepository, times(1)).save(argThat(savedRental ->
                 savedRental.getId().equals(rental1Id) &&
                 savedRental.getStatusEnum() == RentalStatus.CANCELLED
         ));
          verify(rentalRepository, never()).deleteById(anyString()); // Pastikan delete tidak dipanggil
     }

    @Test
    void testCancelRental_whenIdNotExists_shouldThrowNoSuchElementException() {
        when(rentalRepository.findById(rental1Id)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            rentalService.cancelRental(rental1Id);
        }, "Should throw NoSuchElementException when rental ID not found for cancellation");

        verify(rentalRepository, times(1)).findById(rental1Id);
        verify(rentalRepository, never()).deleteById(anyString());
        // Verify: Pastikan save TIDAK pernah dipanggil
        verify(rentalRepository, never()).save(any(Rental.class));
    }
}
