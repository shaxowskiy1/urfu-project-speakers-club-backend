package ru.shaxowskiy.javaspeakerclub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shaxowskiy.javaspeakerclub.dto.LectureCreateRequest;
import ru.shaxowskiy.javaspeakerclub.dto.LectureResponse;
import ru.shaxowskiy.javaspeakerclub.dto.LectureUpdateRequest;
import ru.shaxowskiy.javaspeakerclub.service.LectureService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @GetMapping("/by-talk/{talkId}")
    public List<LectureResponse> findByTalkId(@PathVariable UUID talkId) {
        return lectureService.findByTalkId(talkId);
    }

    @GetMapping("/by-speaker/{speakerId}")
    public List<LectureResponse> findBySpeakerId(@PathVariable Long speakerId) {
        return lectureService.findBySpeakerId(speakerId);
    }

    @GetMapping("/{id}")
    public LectureResponse findById(@PathVariable UUID id) {
        return lectureService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LectureResponse create(@RequestBody LectureCreateRequest request) {
        return lectureService.create(request);
    }

    @PutMapping("/{id}")
    public LectureResponse update(@PathVariable UUID id, @RequestBody LectureUpdateRequest request) {
        return lectureService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lectureService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
