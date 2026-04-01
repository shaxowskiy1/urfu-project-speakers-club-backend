package ru.shaxowskiy.javaspeakerclub.dto;

import java.time.LocalDateTime;

public record TalkUpdateRequest(
        String topic,
        LocalDateTime talkDate,
        String conferenceType,
        String format,
        String activityName,
        LocalDateTime activityDate
) {}
