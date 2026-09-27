package com.example.backend;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setApproved(rs.getInt("approved") == 1);
        user.setCreatedAt(rs.getString("created_at"));
        return user;
    };

    public Optional<User> findByUsername(String username) {
        return jdbcTemplate
                .query("SELECT * FROM users WHERE username = ?", userRowMapper, username)
                .stream()
                .findFirst();
    }

    public void save(User user) {
        jdbcTemplate.update(
                "INSERT INTO users (username, password, approved) VALUES (?, ?, ?)",
                user.getUsername(),
                user.getPassword(),
                user.isApproved() ? 1 : 0
        );
    }

    public long count() {
        Long c = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Long.class);
        return c == null ? 0 : c;
    }
}