package com.techarch.TafDatastoreService.controller;

import com.techarch.TafDatastoreService.entity.Booking;
import com.techarch.TafDatastoreService.entity.Flight;
import com.techarch.TafDatastoreService.entity.User;
import com.techarch.TafDatastoreService.repository.BookingRepository;
import com.techarch.TafDatastoreService.repository.FlightRepository;
import com.techarch.TafDatastoreService.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TafDatastoreController {
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private FlightRepository flightRepository;



    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(updatedUser.getUsername());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        return userRepository.save(user);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }


    //---------Flight---------------------------

    // Get all flights
    @GetMapping("/flights")
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    // Get flight by ID
    @GetMapping("/flights/{flightId}")
    public ResponseEntity<Flight> getFlightById(@PathVariable Long flightId) {
        return flightRepository.findById(flightId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Add a new flight

    @PostMapping("/flights")
    public ResponseEntity<Flight> addFlight(@RequestBody Flight flight) {
        // flight.setCreatedAt(LocalDateTime.now());
        return ResponseEntity.ok(flightRepository.save(flight));
    }

    // Update an existing flight
    @PutMapping("/flights/{flightId}")
    public ResponseEntity<Flight> updateFlight(@PathVariable Long flightId, @RequestBody Flight flight) {
        if (flightRepository.existsById(flightId)) {
            flight.setId(flightId);
            // flight.setUpdatedAt(LocalDateTime.now());
            return ResponseEntity.ok(flightRepository.save(flight));
        }
        return ResponseEntity.notFound().build();
    }

    // Delete a flight
    @DeleteMapping("/flights/{flightId}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long flightId) {
        if (flightRepository.existsById(flightId)) {
            flightRepository.deleteById(flightId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // ----------------------Booking---------------------

    // Create a new booking
    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        // Fetch the flight by ID
        Optional<Flight> flightOpt = flightRepository.findById(booking.getFlight().getId());
        if (flightOpt.isEmpty() || flightOpt.get().getAvailableSeats() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Flight unavailable
        }

        // Fetch the user by ID
        Optional<User> userOpt = userRepository.findById(booking.getUser().getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // User not found
        }

        // Reduce available seats
        Flight flight = flightOpt.get();
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        flightRepository.save(flight);

        // Set booking details
        booking.setUser(userOpt.get());
        booking.setFlight(flight);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setStatus("Booked");

        // Save the booking
        Booking savedBooking = bookingRepository.save(booking);

        // Return the saved booking with all details
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
    }

    // Get a booking by ID
    @GetMapping("/bookings/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        return booking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Get bookings for a specific user
    @GetMapping("/bookings/user/{userId}")
    public ResponseEntity<List<Booking>> getBookingsByUser(@PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Booking> bookings = bookingRepository.findByUser(user.get());
        return ResponseEntity.ok(bookings);
    }

    // Cancel a booking by ID
    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Booking bookingToUpdate = booking.get();
        bookingToUpdate.setStatus("Cancelled");
        bookingToUpdate.setUpdatedAt(LocalDateTime.now());
        bookingRepository.save(bookingToUpdate);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    // Retrieve all bookings
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }


}
