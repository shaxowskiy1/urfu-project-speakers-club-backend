package ru.shaxowskiy.javaspeakerclub.dto;

import java.math.BigDecimal;

public record NpsUpdateRequest(
        BigDecimal nps
) {}
