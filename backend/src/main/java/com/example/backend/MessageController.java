package com.example.backend;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MessageController {

    private final MessageRepository repository;
    private final FcmTokenRepository tokenRepository;
    private final MessageWebSocketHandler wsHandler;
    private final FcmService fcmService;

    // ✅ Все зависимости теперь внедряются через конструктор
    public MessageController(MessageRepository repository,
                             FcmTokenRepository tokenRepository,
                             MessageWebSocketHandler wsHandler,
                             FcmService fcmService) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.wsHandler = wsHandler;
        this.fcmService = fcmService;
    }

    // ============================
    // Сообщения
    // ============================

    @PostMapping("/messages")
    public ResponseEntity<?> create(@RequestBody Map<String, String> payload) {
        String text = payload.get("text");
        if (text == null || text.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
        }

        // 1. Сохраняем сообщение в базу
        repository.save(text);

        // 2. Получаем только что сохранённое сообщение (с id и createdAt)
        Message last = repository.findLast();

        // 3. Рассылаем через WebSocket всем, у кого открыт экран
        wsHandler.broadcast(last);

        // ✅ 4. Отправляем FCM-push на все зарегистрированные устройства
        List<String> tokens = tokenRepository.findAll();
        for (String token : tokens) {
            fcmService.sendNotification(token, "Новое сообщение", last.getText());
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/messages")
    public List<Message> list() {
        return repository.findAll();
    }

    // ============================
    // Регистрация FCM-токена
    // ============================

    @PostMapping("/register-token")
    public ResponseEntity<?> registerToken(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }
        tokenRepository.save(token);
        System.out.println("Зарегистрирован FCM-токен: " + token);
        return ResponseEntity.ok(Map.of("success", true));
    }
}