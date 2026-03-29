package ru.shaxowskiy.javaspeakerclub.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SpeakerResponse(
        Long id,
        String username,
        BigDecimal nps,
        LocalDateTime createdDate,
        List<RoleResponse> roles
) {}
