package com.nikitsya.billing.assistant;

import java.util.List;

public record ChatRequest(List<Message> messages) {
    public void validate() {
        if (messages == null || messages.isEmpty() || messages.size() > 9 || messages.size() % 2 == 0) {
            throw ChatException.invalidRequest();
        }
        int characters = 0;
        for (int index = 0; index < messages.size(); index++) {
            Message message = messages.get(index);
            String expectedRole = index % 2 == 0 ? "user" : "assistant";
            int limit = expectedRole.equals("user") ? 2000 : 8000;
            if (message == null || !expectedRole.equals(message.role()) || message.content() == null
                    || message.content().isBlank() || message.content().length() > limit) {
                throw ChatException.invalidRequest();
            }
            characters += message.content().length();
        }
        if (characters > 12000) {
            throw ChatException.invalidRequest();
        }
    }

    public record Message(String role, String content) {
    }
}
