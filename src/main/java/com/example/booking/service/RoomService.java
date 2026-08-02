package com.example.booking.service;

import com.example.booking.entity.Room;

import java.util.List;

public interface RoomService {

    Room createRoom(Room room);

    List<Room> getAllRooms();
}
