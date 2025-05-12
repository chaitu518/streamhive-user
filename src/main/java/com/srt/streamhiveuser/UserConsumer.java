package com.srt.streamhiveuser;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "register-user", groupId = "demo-consumer-group")
    public void consume(ConsumerRecord<String, String> record) {
        String message = record.value();
        try {
            JsonNode node = objectMapper.readTree(message);
            String userId = node.has("id") ? node.get("id").asText() : "N/A";
            String userEmail = node.has("user") && node.get("user").has("email")
                    ? node.get("user").get("email").asText() : "N/A";

            System.out.println("Received User ID: " + userId);
            System.out.println("Received User Email: " + userEmail);
            System.out.println("-----------------------------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
