package ru.shaxowskiy.javaspeakerclub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.shaxowskiy.javaspeakerclub.dto.LectureResponse;
import ru.shaxowskiy.javaspeakerclub.dto.TalkCreateRequest;
import ru.shaxowskiy.javaspeakerclub.dto.TalkResponse;
import ru.shaxowskiy.javaspeakerclub.dto.TalkUpdateRequest;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.LecturesRecord;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.TalksRecord;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.UsersRecord;
import ru.shaxowskiy.javaspeakerclub.repository.LectureRepository;
import ru.shaxowskiy.javaspeakerclub.repository.TalkRepository;
import ru.shaxowskiy.javaspeakerclub.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TalkService {

    private final TalkRepository talkRepository;
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TalkResponse> findAll() {
        return talkRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TalkResponse findById(UUID id) {
        TalksRecord record = talkRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Talk not found: " + id));
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<TalkResponse> findByConferenceType(String conferenceType) {
        return talkRepository.findByConferenceType(conferenceType).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TalkResponse create(TalkCreateRequest request) {
        userRepository.findById(request.speakerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Speaker not found: " + request.speakerId()));

        TalksRecord record = talkRepository.create(
                request.speakerId(),
                request.topic(),
                request.talkDate(),
                request.conferenceType(),
                request.format(),
                request.activityName(),
                request.activityDate()
        );
        return toResponse(record);
    }

    @Transactional
    public TalkResponse update(UUID id, TalkUpdateRequest request) {
        TalksRecord updated = talkRepository.update(
                id,
                request.topic(),
                request.talkDate(),
                request.conferenceType(),
                request.format(),
                request.activityName(),
                request.activityDate()
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Talk not found: " + id));

        return toResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        if (!talkRepository.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Talk not found: " + id);
        }
    }

    private TalkResponse toResponse(TalksRecord record) {
        String speakerUsername = userRepository.findById(record.getSpeakerId())
                .map(UsersRecord::getUsername)
                .orElse(null);

        List<LectureResponse> lectures = lectureRepository.findByTalkId(record.getId()).stream()
                .map(this::toLectureResponse)
                .toList();

        return new TalkResponse(
                record.getId(),
                record.getSpeakerId(),
                speakerUsername,
                record.getTopic(),
                record.getTalkDate(),
                record.getConferenceType(),
                record.getFormat(),
                record.getActivityName(),
                record.getActivityDate(),
                record.getCreatedDate(),
                lectures
        );
    }

    private LectureResponse toLectureResponse(LecturesRecord record) {
        return new LectureResponse(
                record.getId(),
                record.getTitle(),
                record.getTalkId(),
                record.getSpeakerId(),
                record.getMediaS3Key(),
                record.getCreatedDate()
        );
    }
}
