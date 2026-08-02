package com.example.booking.service;

import com.example.booking.entity.User;

import java.util.List;

public interface UserService {

    User createUser(User user);

    List<User> getAllUsers();
}
