package com.example.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageRepository repository;
    private final MessageWebSocketHandler wsHandler;

    public MessageController(MessageRepository repository, MessageWebSocketHandler wsHandler) {
        this.repository = repository;
        this.wsHandler = wsHandler;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
        }
        repository.save(text);

        // Получаем только что сохранённое сообщение и рассылаем его
        Message last = repository.findLast();
        wsHandler.broadcast(last);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping
    public List<Message> list() {
        return repository.findAll();
    }
}