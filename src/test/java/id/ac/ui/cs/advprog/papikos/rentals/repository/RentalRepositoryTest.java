package id.ac.ui.cs.advprog.papikos.rentals.repository;

import id.ac.ui.cs.advprog.papikos.rentals.model.KosDummy;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import id.ac.ui.cs.advprog.papikos.rentals.model.TenantDummy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RentalRepositoryTest {

    RentalRepository rentalRepository;
    Rental rental1;
    Rental rental2;
    KosDummy dummyKos;
    TenantDummy dummyTenant;

    @BeforeEach
    void setUp() {
        rentalRepository = new RentalRepository();

        // Data dummy
        dummyKos = new KosDummy();
        dummyKos.setId(UUID.randomUUID().toString());
        dummyTenant = new TenantDummy();
        dummyTenant.setId(UUID.randomUUID().toString());

        rental1 = new Rental(UUID.randomUUID().toString(), dummyKos, dummyTenant, "Penyewa Satu", "081111", "2024-01-01", 3);
        rental2 = new Rental(UUID.randomUUID().toString(), dummyKos, dummyTenant, "Penyewa Dua", "082222", "2024-02-01", 6, "CONFIRMED");
    }

    @Test
    void testSaveNewRental_shouldAddRentalToList() {
        Rental savedRental = rentalRepository.save(rental1);
        assertNotNull(savedRental);
        assertEquals(rental1.getId(), savedRental.getId());

        Optional<Rental> foundRental = rentalRepository.findById(rental1.getId());
        assertTrue(foundRental.isPresent());
        assertEquals(rental1.getId(), foundRental.get().getId());
        assertEquals(1, rentalRepository.findAll().size());
    }

    @Test
    void testSaveExistingRental_shouldUpdateRentalInList() {
        rentalRepository.save(rental1); // Simpan dulu

        // Buat perubahan (misalnya, status)
        rental1.setStatus("CONFIRMED");
        Rental updatedRental = rentalRepository.save(rental1); // Simpan lagi dengan ID yang sama

        assertNotNull(updatedRental);
        assertEquals("CONFIRMED", updatedRental.getStatus()); // Cek data yang diupdate

        Optional<Rental> foundRental = rentalRepository.findById(rental1.getId());
        assertTrue(foundRental.isPresent());
        assertEquals("CONFIRMED", foundRental.get().getStatus());
        assertEquals(1, rentalRepository.findAll().size()); // Pastikan jumlah tidak bertambah
    }

    @Test
    void testFindByIdWithValidId_shouldReturnRental() {
        rentalRepository.save(rental1);
        rentalRepository.save(rental2);

        Optional<Rental> foundRental = rentalRepository.findById(rental1.getId());
        assertTrue(foundRental.isPresent());
        assertEquals(rental1.getId(), foundRental.get().getId());
    }

    @Test
    void testFindByIdWithInvalidId_shouldReturnEmptyOptional() {
        rentalRepository.save(rental1);
        String invalidId = UUID.randomUUID().toString();

        Optional<Rental> foundRental = rentalRepository.findById(invalidId);
        assertFalse(foundRental.isPresent());
    }

    @Test
    void testFindAll_shouldReturnAllRentals() {
        rentalRepository.save(rental1);
        rentalRepository.save(rental2);

        List<Rental> allRentals = rentalRepository.findAll();
        assertNotNull(allRentals);
        assertEquals(2, allRentals.size());
        assertTrue(allRentals.stream().anyMatch(r -> r.getId().equals(rental1.getId())));
        assertTrue(allRentals.stream().anyMatch(r -> r.getId().equals(rental2.getId())));
    }

    @Test
    void testFindAllWhenEmpty_shouldReturnEmptyList() {
        List<Rental> allRentals = rentalRepository.findAll();
        assertNotNull(allRentals);
        assertTrue(allRentals.isEmpty());
    }

    @Test
    void testDeleteByIdWithValidId_shouldRemoveRental() {
        rentalRepository.save(rental1);
        rentalRepository.save(rental2);
        assertEquals(2, rentalRepository.findAll().size());

        boolean deleted = rentalRepository.deleteById(rental1.getId());
        assertTrue(deleted); // Pastikan method mengindikasikan sukses

        assertEquals(1, rentalRepository.findAll().size()); // Ukuran berkurang
        assertFalse(rentalRepository.findById(rental1.getId()).isPresent()); // Pastikan tidak ditemukan lagi
        assertTrue(rentalRepository.findById(rental2.getId()).isPresent()); // Pastikan yang lain masih ada
    }

    @Test
    void testDeleteByIdWithInvalidId_shouldDoNothingAndReturnFalse() {
        rentalRepository.save(rental1);
        rentalRepository.save(rental2);
        assertEquals(2, rentalRepository.findAll().size());
        String invalidId = UUID.randomUUID().toString();

        boolean deleted = rentalRepository.deleteById(invalidId);
        assertFalse(deleted); // Pastikan method mengindikasikan gagal/tidak ada

        assertEquals(2, rentalRepository.findAll().size()); // Ukuran tidak berubah
        assertTrue(rentalRepository.findById(rental1.getId()).isPresent()); // Data masih ada
        assertTrue(rentalRepository.findById(rental2.getId()).isPresent()); // Data masih ada
    }
}
