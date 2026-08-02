package com.example.booking.service;

import com.example.booking.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    Booking createBooking(Long roomId, Long userId, LocalDateTime startTime, LocalDateTime endTime);

    List<Booking> getAllBookings();

    Booking cancelBooking(Long bookingId);

    Booking rescheduleBooking(Long bookingId, LocalDateTime newStartTime, LocalDateTime newEndTime);
}
