package ru.shaxowskiy.javaspeakerclub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.shaxowskiy.javaspeakerclub.dto.RoleCreateRequest;
import ru.shaxowskiy.javaspeakerclub.dto.RoleResponse;
import ru.shaxowskiy.javaspeakerclub.dto.SpeakerResponse;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.SpeakerRolesRecord;
import ru.shaxowskiy.javaspeakerclub.jooq.tables.records.UsersRecord;
import ru.shaxowskiy.javaspeakerclub.repository.RoleRepository;
import ru.shaxowskiy.javaspeakerclub.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        userRepository.findById(request.speakerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Speaker not found: " + request.speakerId()));

        SpeakerRolesRecord record = roleRepository.create(request.name(), request.speakerId());
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findBySpeakerId(Long speakerId) {
        userRepository.findById(speakerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Speaker not found: " + speakerId));

        return roleRepository.findBySpeakerId(speakerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SpeakerResponse> findSpeakersByRole(String roleNamePart) {
        List<UsersRecord> speakers = (roleNamePart == null || roleNamePart.isBlank())
                ? roleRepository.findAllSpeakersWithRoles()
                : roleRepository.findSpeakersByRoleNameContaining(roleNamePart);

        return speakers.stream()
                .map(this::toSpeakerResponse)
                .toList();
    }

    private SpeakerResponse toSpeakerResponse(UsersRecord user) {
        List<RoleResponse> roles = roleRepository.findBySpeakerId(user.getId()).stream()
                .map(this::toResponse)
                .toList();
        return new SpeakerResponse(
                user.getId(),
                user.getUsername(),
                user.getCreatedDate(),
                roles
        );
    }

    @Transactional
    public void delete(Long id) {
        if (!roleRepository.deleteById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found: " + id);
        }
    }

    private RoleResponse toResponse(SpeakerRolesRecord record) {
        return new RoleResponse(
                record.getId(),
                record.getName(),
                record.getSpeakerId(),
                record.getCreatedDate()
        );
    }
}
