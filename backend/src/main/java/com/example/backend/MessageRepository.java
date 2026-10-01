package com.example.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MessageRepository {

    private final JdbcTemplate jdbcTemplate;

    // Внедрение JdbcTemplate через конструктор
    public MessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Маппер строки результата в объект Message
    private final RowMapper<Message> rowMapper = (rs, rowNum) -> {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setText(rs.getString("text"));
        message.setCreatedAt(toIsoUtc(rs.getString("created_at")));
        return message;
    };

    // SQLite CURRENT_TIMESTAMP хранит время в UTC как "yyyy-MM-dd HH:mm:ss" без зоны.
    // Добавляем 'T' и суффикс 'Z', чтобы клиент однозначно понял, что это UTC,
    // и перевёл время в локальную таймзону пользователя.
    private static String toIsoUtc(String raw) {
        if (raw == null || raw.isBlank() || raw.endsWith("Z") || raw.indexOf('T') >= 0) {
            return raw;
        }
        return raw.replace(' ', 'T') + "Z";
    }

    // Сохранение нового сообщения
    public void save(String text) {
        jdbcTemplate.update("INSERT INTO messages (text) VALUES (?)", text);
    }

    // Получение всех сообщений (сначала новые)
    public List<Message> findAll() {
        return jdbcTemplate.query("SELECT * FROM messages ORDER BY id DESC", rowMapper);
    }

    public Message findLast() {
    return jdbcTemplate.queryForObject(
        "SELECT * FROM messages ORDER BY id DESC LIMIT 1", rowMapper);
    }
}