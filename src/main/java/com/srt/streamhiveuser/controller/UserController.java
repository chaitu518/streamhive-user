package com.srt.streamhiveuser.controller;


import com.srt.streamhiveuser.model.UserProfile;
import com.srt.streamhiveuser.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getUser(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestParam String username,
                                           @RequestParam String bio) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserProfile updated = userService.updateProfile(email, bio, username);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String imageUrl = userService.uploadProfileImage(email, file);
            return ResponseEntity.ok("Image uploaded successfully: " + imageUrl);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }
}
