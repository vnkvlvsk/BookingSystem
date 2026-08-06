package com.example.booking.service;

import com.example.booking.entity.Room;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {

    Room createRoom(Room room);

    List<Room> getAllRooms();

    List<Room> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime);
}
