package com.srt.streamhiveuser.repo;


import com.srt.streamhiveuser.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    boolean existsByEmail(String email);

    Optional<UserProfile> findByEmail(String email);
}
