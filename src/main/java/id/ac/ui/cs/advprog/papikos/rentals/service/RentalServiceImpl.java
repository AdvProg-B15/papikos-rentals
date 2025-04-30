package id.ac.ui.cs.advprog.papikos.rentals.service;

import id.ac.ui.cs.advprog.papikos.rentals.enums.RentalStatus;
import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;
import id.ac.ui.cs.advprog.papikos.rentals.repository.RentalRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;

    public RentalServiceImpl(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    @Override
    public Rental createRental(Rental rental) {
        if (rental == null || rental.getId() == null) {
            throw new IllegalArgumentException("Rental data or ID cannot be null for creation.");
        }
        Optional<Rental> existingRental = rentalRepository.findById(rental.getId());
        if (existingRental.isPresent()) {
            throw new IllegalArgumentException("Rental with ID " + rental.getId() + " already exists.");
        }
        return rentalRepository.save(rental);
    }

    @Override
    public Rental updateRental(String rentalId, Rental updatedRentalData) {
        if (rentalId == null || updatedRentalData == null) {
            throw new IllegalArgumentException("Rental ID and updated data cannot be null for update.");
        }
        // Pastikan ID di path cocok dengan ID di body jika perlu
         if (!rentalId.equals(updatedRentalData.getId())) {
             throw new IllegalArgumentException("Path ID does not match rental data ID.");
         }

        Rental existingRental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NoSuchElementException("Rental with ID " + rentalId + " not found for update."));

        existingRental.setFullName(updatedRentalData.getFullName());
        existingRental.setPhoneNumber(updatedRentalData.getPhoneNumber());
        existingRental.setDurationMonths(updatedRentalData.getDurationMonths());
        existingRental.setStatus(updatedRentalData.getStatus());

        return rentalRepository.save(existingRental);
    }

     @Override
     public void cancelRental(String rentalId) {
         if (rentalId == null) {
              throw new IllegalArgumentException("Rental ID cannot be null for cancellation.");
         }
         Rental rentalToCancel = rentalRepository.findById(rentalId)
                 .orElseThrow(() -> new NoSuchElementException("Rental with ID " + rentalId + " not found for cancellation."));

         // Ubah status menjadi CANCELLED
         rentalToCancel.setStatus(RentalStatus.CANCELLED.name());

         // Simpan perubahan
         rentalRepository.save(rentalToCancel);
     }

    @Override
    public Rental findRentalById(String rentalId) {
        if (rentalId == null) {
            throw new IllegalArgumentException("Rental ID cannot be null for finding.");
        }
        return rentalRepository.findById(rentalId)
                .orElseThrow(() -> new NoSuchElementException("Rental with ID " + rentalId + " not found."));
    }

    @Override
    public List<Rental> findAllRentals() {
        return rentalRepository.findAll();
    }
}
