package ru.shaxowskiy.javaspeakerclub.service;

import ru.shaxowskiy.javaspeakerclub.entity.User;

public interface UserService {
    User registerUser(String username, String password);
    User authenticateUser(String username, String password);
}