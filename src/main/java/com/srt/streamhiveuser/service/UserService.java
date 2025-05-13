package com.srt.streamhiveuser.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.srt.streamhiveuser.model.UserProfile;
import com.srt.streamhiveuser.repo.UserProfileRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserProfileRepository userRepo;
    private final MinioClient minioClient;
    private final ObjectMapper objectMapper;

    @Value("${minio.bucket}")
    private String bucket;

    public UserService(UserProfileRepository userRepo, MinioClient minioClient, ObjectMapper objectMapper) {
        this.userRepo = userRepo;
        this.minioClient = minioClient;
        this.objectMapper = objectMapper;
    }

    public Optional<UserProfile> getUser(String email) {
        return userRepo.findByEmail(email);
    }

    public UserProfile updateProfile(String email, String bio, String username) {
        UserProfile user = userRepo.findByEmail(email).orElseThrow();
        user.setBio(bio);
        user.setUsername(username);
        return userRepo.save(user);
    }

    public String uploadProfileImage(String email, MultipartFile file) throws Exception {
        String objectName = "profile_" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        String imageUrl = bucket + "/" + objectName;
        UserProfile user = userRepo.findByEmail(email).orElseThrow();
        user.setProfileImageUrl(imageUrl);
        userRepo.save(user);

        return imageUrl;
    }

    @KafkaListener(topics = "register-user", groupId = "demo-consumer-group")
    public void handleUserRegistered(ConsumerRecord<String, String> record) {
        String message = record.value();
        try {
            JsonNode node = objectMapper.readTree(message);
            String userId = node.has("id") ? node.get("id").asText() : "N/A";
            String email = node.has("user") && node.get("user").has("email")
                    ? node.get("user").get("email").asText() : "N/A";
            if (!Objects.equals(email, "N/A") && !userRepo.existsByEmail(email)) {
                UserProfile profile = new UserProfile();
                profile.setEmail(email);
                profile.setUsername(email.split("@")[0]);
                profile.setBio("legends will born again");
                userRepo.save(profile);
            }
            System.out.println("Received User ID: " + userId);
            System.out.println("Received User Email: " + email);
            System.out.println("-----------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
