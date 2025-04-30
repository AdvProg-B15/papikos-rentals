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
        return null;
    }

    public Optional<Rental> findById(String id) {
        return Optional.empty();
    }

    public List<Rental> findAll() {
        return new ArrayList<>();
    }

    public boolean deleteById(String id) {
        return false;
    }
}