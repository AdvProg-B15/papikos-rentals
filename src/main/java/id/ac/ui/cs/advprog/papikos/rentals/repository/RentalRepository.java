package id.ac.ui.cs.advprog.papikos.rentals.repository;

import id.ac.ui.cs.advprog.papikos.rentals.model.Rental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RentalRepository {

    private final Map<String, Rental> rentals = new HashMap<>();

    public Rental save(Rental rental) {
        if (rental == null || rental.getId() == null || rental.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Rental or Rental ID cannot be null or empty for saving.");
        }
        rentals.put(rental.getId(), rental);
        return rental;
    }

    public Optional<Rental> findById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(rentals.get(id));
    }

    public List<Rental> findAll() {
        return new ArrayList<>(rentals.values());
    }

    public boolean deleteById(String id) {
        if (id == null) {
            return false;
        }
        return rentals.remove(id) != null;
    }
}