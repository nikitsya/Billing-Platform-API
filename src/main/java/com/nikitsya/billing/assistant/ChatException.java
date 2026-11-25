package com.nikitsya.billing.assistant;

import org.springframework.http.HttpStatus;

public class ChatException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ChatException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ChatException invalidRequest() {
        return new ChatException(HttpStatus.BAD_REQUEST, "INVALID_CHAT_REQUEST",
                "Please send a question of up to 2,000 characters with a short conversation history.");
    }

    public static ChatException unavailable() {
        return new ChatException(HttpStatus.SERVICE_UNAVAILABLE, "ASSISTANT_UNAVAILABLE",
                "The project assistant is temporarily unavailable. Please try again later.");
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
