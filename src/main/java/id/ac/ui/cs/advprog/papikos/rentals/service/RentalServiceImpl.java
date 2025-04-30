package id.ac.ui.cs.advprog.papikos.rentals.service;

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
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Rental updateRental(String rentalId, Rental updatedRentalData) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void cancelRental(String rentalId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Rental findRentalById(String rentalId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<Rental> findAllRentals() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
