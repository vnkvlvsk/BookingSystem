package com.example.booking.repository;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.Role;
import com.example.booking.entity.Room;
import com.example.booking.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private UserRepository userRepository;

    private Room room;
    private User user;

    @BeforeEach
    void setUp() {
        room = roomRepository.save(new Room("Conference Room A", 8));
        user = userRepository.save(new User("Ivan", "ivan@example.com", Role.USER));
    }

    @Test
    void findsOverlappingConfirmedBooking() {
        bookingRepository.save(new Booking(room, user,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                BookingStatus.CONFIRMED));

        List<Booking> conflicts = bookingRepository.findConflicting(
                room.getId(),
                LocalDateTime.of(2026, 8, 1, 11, 0),
                LocalDateTime.of(2026, 8, 1, 13, 0),
                null);

        assertThat(conflicts).hasSize(1);
    }

    @Test
    void ignoresNonOverlappingBooking() {
        bookingRepository.save(new Booking(room, user,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 11, 0),
                BookingStatus.CONFIRMED));

        List<Booking> conflicts = bookingRepository.findConflicting(
                room.getId(),
                LocalDateTime.of(2026, 8, 1, 11, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                null);

        assertThat(conflicts).isEmpty();
    }

    @Test
    void ignoresCancelledBooking() {
        bookingRepository.save(new Booking(room, user,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                BookingStatus.CANCELLED));

        List<Booking> conflicts = bookingRepository.findConflicting(
                room.getId(),
                LocalDateTime.of(2026, 8, 1, 11, 0),
                LocalDateTime.of(2026, 8, 1, 13, 0),
                null);

        assertThat(conflicts).isEmpty();
    }

    @Test
    void excludesGivenBookingId() {
        Booking booking = bookingRepository.save(new Booking(room, user,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                BookingStatus.CONFIRMED));

        List<Booking> conflicts = bookingRepository.findConflicting(
                room.getId(),
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                booking.getId());

        assertThat(conflicts).isEmpty();
    }

    @Test
    void ignoresOtherRooms() {
        Room otherRoom = roomRepository.save(new Room("Small Room B", 4));
        bookingRepository.save(new Booking(otherRoom, user,
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                BookingStatus.CONFIRMED));

        List<Booking> conflicts = bookingRepository.findConflicting(
                room.getId(),
                LocalDateTime.of(2026, 8, 1, 10, 0),
                LocalDateTime.of(2026, 8, 1, 12, 0),
                null);

        assertThat(conflicts).isEmpty();
    }
}
