package ru.shaxowskiy.javaspeakerclub.dto;

public record LectureUpdateRequest(
        String title,
        String mediaS3Key
) {}
