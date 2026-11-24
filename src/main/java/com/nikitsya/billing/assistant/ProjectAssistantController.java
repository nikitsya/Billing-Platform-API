package com.nikitsya.billing.assistant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikitsya.billing.common.api.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/project-assistant")
public class ProjectAssistantController {
    private static final int MAX_BODY_BYTES = 65536;
    private final ProjectAssistantService service;
    private final ObjectMapper mapper;

    public ProjectAssistantController(ProjectAssistantService service, ObjectMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping(path = "/messages", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> answer(HttpServletRequest request) throws IOException {
        byte[] body = request.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
        if (body.length > MAX_BODY_BYTES) {
            throw new ChatException(HttpStatus.PAYLOAD_TOO_LARGE, "CHAT_REQUEST_TOO_LARGE",
                    "Please shorten your conversation and try again.");
        }
        try {
            ChatRequest chat = mapper.readValue(body, ChatRequest.class);
            if (chat == null) throw ChatException.invalidRequest();
            chat.validate();
            return ResponseEntity.ok().header("Cache-Control", "no-store")
                    .body(service.answer(chat));
        } catch (JsonProcessingException exception) {
            throw ChatException.invalidRequest();
        }
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatError(ChatException exception) {
        return ResponseEntity.status(exception.status()).header("Cache-Control", "no-store")
                .body(new ErrorResponse(exception.code(), exception.getMessage()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleQuotaStorageError() {
        return handleChatError(ChatException.unavailable());
    }
}
