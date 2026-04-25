package ru.shaxowskiy.javaspeakerclub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.shaxowskiy.javaspeakerclub.dto.AuthResponse;
import ru.shaxowskiy.javaspeakerclub.entity.User;
import ru.shaxowskiy.javaspeakerclub.security.AppRole;
import ru.shaxowskiy.javaspeakerclub.service.UserService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final UserService userService;

    @PostMapping("/sign-on")
    public ResponseEntity<AuthResponse> register(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user.getUsername(), user.getPassword());
            return ResponseEntity.ok(toAuthResponse(registeredUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> login(@RequestBody User user) {
        User authenticatedUser = userService.authenticateUser(user.getUsername(), user.getPassword());
        if (authenticatedUser != null) {
            return ResponseEntity.ok(toAuthResponse(authenticatedUser));
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // In stateless JWT, logout is handled on the client
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    public List<String> roles() {
        return Arrays.stream(AppRole.values())
                .map(Enum::name)
                .toList();
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRoles(),
                user.getCreatedDate(),
                user.getLastModifiedDate()
        );
    }
}