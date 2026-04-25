package ru.shaxowskiy.javaspeakerclub.dto;

import java.time.LocalDateTime;

public record RoleResponse(
        Long id,
        String name,
        Long speakerId,
        LocalDateTime createdDate
) {}
