package id.ac.ui.cs.advprog.papikos.rentals.service;

import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;

import java.util.List;
import java.util.Optional;

public interface RentalService {
    Rental createRental(Rental rental);
    Rental updateRental(String rentalId, Rental updatedRentalData);
    void cancelRental(String rentalId); // void karena hanya aksi
    Rental findRentalById(String rentalId);
    List<Rental> findAllRentals();
}
