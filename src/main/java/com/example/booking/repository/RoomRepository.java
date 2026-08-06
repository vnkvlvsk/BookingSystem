package com.example.booking.repository;

import com.example.booking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r NOT IN (" +
            "SELECT b.room FROM Booking b " +
            "WHERE b.status = com.example.booking.entity.BookingStatus.CONFIRMED " +
            "AND b.startTime < :endTime AND b.endTime > :startTime)")
    List<Room> findAvailable(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}
