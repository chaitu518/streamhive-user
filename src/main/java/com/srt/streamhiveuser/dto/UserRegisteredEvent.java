package com.srt.streamhiveuser.dto;


import lombok.Data;

import java.util.Map;

@Data
public class UserRegisteredEvent {
    private String id;
    private Map<String, String> user; // user.email
}