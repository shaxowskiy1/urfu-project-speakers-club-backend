package ru.shaxowskiy.javaspeakerclub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.shaxowskiy.javaspeakerclub.dto.LectureResponse;
import ru.shaxowskiy.javaspeakerclub.service.LectureService;
import ru.shaxowskiy.javaspeakerclub.service.MinioService;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final LectureService lectureService;
    private final MinioService minioService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public LectureResponse uploadMedia(
            @RequestParam("lectureTitle") String lectureTitle,
            @RequestPart("file") MultipartFile file) {
        return lectureService.uploadMedia(lectureTitle, file);
    }

    @GetMapping("/download/{lectureId}")
    public ResponseEntity<InputStreamResource> downloadMedia(@PathVariable UUID lectureId) {
        LectureResponse lecture = lectureService.findById(lectureId);

        if (lecture.mediaS3Key() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Lecture has no media attached: " + lectureId);
        }

        String filename = extractFilename(lecture.mediaS3Key());
        InputStream stream = minioService.getFile(lecture.mediaS3Key());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(stream));
    }

    @GetMapping("/url/{lectureId}")
    public ResponseEntity<String> getPresignedUrl(@PathVariable UUID lectureId) {
        String url = lectureService.getMediaUrl(lectureId);
        return ResponseEntity.ok(url);
    }

    @DeleteMapping("/{lectureId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID lectureId) {
        lectureService.deleteMedia(lectureId);
        return ResponseEntity.noContent().build();
    }

    private String extractFilename(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return "download";
        }
        if (!objectKey.contains("/")) {
            return objectKey;
        }
        String name = objectKey.substring(objectKey.lastIndexOf('/') + 1);
        int uuidSeparator = name.indexOf('_');
        return uuidSeparator > 0 ? name.substring(uuidSeparator + 1) : name;
    }
}
