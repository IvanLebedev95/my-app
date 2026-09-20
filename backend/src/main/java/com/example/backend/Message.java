package com.example.backend;

public class Message {
    private Long id;
    private String text;
    private String createdAt;

    // Конструкторы (не обязательны, но удобны)
    public Message() {}

    public Message(Long id, String text, String createdAt) {
        this.id = id;
        this.text = text;
        this.createdAt = createdAt;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}