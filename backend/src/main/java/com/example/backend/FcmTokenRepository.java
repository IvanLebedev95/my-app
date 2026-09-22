package com.example.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FcmTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public FcmTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Сохранить токен (если уже есть — игнорируем)
    public void save(String token) {
        jdbcTemplate.update(
            "INSERT OR IGNORE INTO fcm_tokens (token) VALUES (?)",
            token
        );
    }

    // Получить все токены
    public List<String> findAll() {
        return jdbcTemplate.queryForList("SELECT token FROM fcm_tokens", String.class);
    }

    // Удалить невалидный токен (например, если FCM вернул ошибку)
    public void delete(String token) {
        jdbcTemplate.update("DELETE FROM fcm_tokens WHERE token = ?", token);
    }
}