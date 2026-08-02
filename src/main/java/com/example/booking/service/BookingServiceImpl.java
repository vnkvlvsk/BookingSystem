package com.example.booking.service;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.Room;
import com.example.booking.entity.User;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RoomRepository;
import com.example.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Booking createBooking(Long roomId, Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + roomId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<Booking> conflicts = bookingRepository.findConflicting(roomId, startTime, endTime, null);
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Room " + roomId + " is already booked for that time range");
        }

        Booking booking = new Booking(room, user, startTime, endTime, BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        booking.setStatus(BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking rescheduleBooking(Long bookingId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        List<Booking> conflicts = bookingRepository.findConflicting(
                booking.getRoom().getId(), newStartTime, newEndTime, bookingId);
        if (!conflicts.isEmpty()) {
            throw new BookingConflictException("Room " + booking.getRoom().getId() + " is already booked for that time range");
        }

        booking.setStartTime(newStartTime);
        booking.setEndTime(newEndTime);
        return bookingRepository.save(booking);
    }
}
