package ru.shaxowskiy.javaspeakerclub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.shaxowskiy.javaspeakerclub.entity.User;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.TalksRecord;
import ru.shaxowskiy.javaspeakerclub.repository.TalkRepository;
import ru.shaxowskiy.javaspeakerclub.repository.UserRepository;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final TalkRepository talkRepository;

    public record TalkDto(
            String id,
            String topic,
            java.time.LocalDateTime talkDate,
            String conferenceType,
            String format,
            String activityName,
            java.time.LocalDateTime activityDate,
            java.time.LocalDateTime createdDate
    ) {}

    public record MeResponse(User me, List<TalkDto> talks) {}

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Not authenticated");
            pd.setType(URI.create("about:blank"));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, pd.getDetail());
        }

        var username = authentication.getName();
        var userRecord = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "User not found");
                    pd.setType(URI.create("about:blank"));
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, pd.getDetail());
                });

        User me = new User();
        me.setId(userRecord.getId());
        me.setUsername(userRecord.getUsername());
        me.setPassword(null);
        me.setCreatedDate(userRecord.getCreatedDate());
        me.setLastModifiedDate(userRecord.getLastModifiedDate());

        List<TalksRecord> talks = talkRepository.findBySpeakerId(userRecord.getId());
        List<TalkDto> talkDtos = talks.stream()
                .map(r -> new TalkDto(
                        r.getId() == null ? null : r.getId().toString(),
                        r.getTopic(),
                        r.getTalkDate(),
                        r.getConferenceType() == null ? null : r.getConferenceType().name(),
                        r.getFormat() == null ? null : r.getFormat().name(),
                        r.getActivityName(),
                        r.getActivityDate(),
                        r.getCreatedDate()
                ))
                .toList();

        return new MeResponse(me, talkDtos);
    }
}

