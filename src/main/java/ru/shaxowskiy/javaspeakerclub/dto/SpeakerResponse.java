package ru.shaxowskiy.javaspeakerclub.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SpeakerResponse(
        Long id,
        String username,
        LocalDateTime createdDate,
        List<RoleResponse> roles
) {}
