package ru.shaxowskiy.javaspeakerclub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.shaxowskiy.javaspeakerclub.dto.LectureCreateRequest;
import ru.shaxowskiy.javaspeakerclub.dto.LectureResponse;
import ru.shaxowskiy.javaspeakerclub.dto.LectureUpdateRequest;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.LecturesRecord;
import ru.shaxowskiy.javaspeakerclub.repository.LectureRepository;
import ru.shaxowskiy.javaspeakerclub.repository.TalkRepository;
import ru.shaxowskiy.javaspeakerclub.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final TalkRepository talkRepository;
    private final UserRepository userRepository;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public List<LectureResponse> findByTalkId(UUID talkId) {
        talkRepository.findById(talkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Talk not found: " + talkId));

        return lectureRepository.findByTalkId(talkId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LectureResponse> findBySpeakerId(Long speakerId) {
        userRepository.findById(speakerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Speaker not found: " + speakerId));

        return lectureRepository.findBySpeakerId(speakerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public LectureResponse findById(UUID id) {
        LecturesRecord record = lectureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found: " + id));
        return toResponse(record);
    }

    @Transactional
    public LectureResponse create(LectureCreateRequest request) {
        talkRepository.findById(request.talkId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Talk not found: " + request.talkId()));

        userRepository.findById(request.speakerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Speaker not found: " + request.speakerId()));

        LecturesRecord record = lectureRepository.create(
                request.title(),
                request.talkId(),
                request.speakerId(),
                request.mediaS3Key()
        );
        return toResponse(record);
    }

    @Transactional
    public LectureResponse update(UUID id, LectureUpdateRequest request) {
        LecturesRecord updated = lectureRepository.update(id, request.title(), request.mediaS3Key())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found: " + id));
        return toResponse(updated);
    }

    @Transactional
    public void delete(UUID id) {
        if (!lectureRepository.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found: " + id);
        }
    }

    @Transactional
    public LectureResponse uploadMedia(String lectureTitle, MultipartFile file) {
        LecturesRecord lecture = lectureRepository.findByTitle(lectureTitle)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Lecture not found by title: " + lectureTitle));

        String oldKey = lecture.getMediaS3Key();
        String objectKey = minioService.uploadFile(lecture.getId().toString(), file);

        LecturesRecord updated = lectureRepository.updateMediaS3Key(lecture.getId(), objectKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update media key for lecture: " + lecture.getId()));

        minioService.deleteFile(oldKey);

        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public String getMediaUrl(UUID lectureId) {
        LecturesRecord lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found: " + lectureId));

        if (lecture.getMediaS3Key() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture has no media attached: " + lectureId);
        }

        return minioService.getPresignedUrl(lecture.getMediaS3Key());
    }

    @Transactional
    public void deleteMedia(UUID lectureId) {
        LecturesRecord lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture not found: " + lectureId));

        if (lecture.getMediaS3Key() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lecture has no media attached: " + lectureId);
        }

        String mediaKey = lecture.getMediaS3Key();
        lectureRepository.updateMediaS3Key(lectureId, null);
        minioService.deleteFile(mediaKey);
    }

    private LectureResponse toResponse(LecturesRecord record) {
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
