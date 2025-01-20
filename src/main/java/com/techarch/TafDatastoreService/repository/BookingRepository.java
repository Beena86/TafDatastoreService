package com.techarch.TafDatastoreService.repository;

import com.techarch.TafDatastoreService.entity.Booking;
import com.techarch.TafDatastoreService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    Optional<Booking> findByIdAndUser(Long bookingId, User user);
    List<Booking> findByUserId(Long userId);

}
