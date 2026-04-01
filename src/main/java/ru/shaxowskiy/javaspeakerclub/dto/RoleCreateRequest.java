package ru.shaxowskiy.javaspeakerclub.dto;

public record RoleCreateRequest(
        String name,
        Long speakerId
) {}
