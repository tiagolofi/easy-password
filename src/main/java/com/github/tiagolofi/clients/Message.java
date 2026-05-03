package com.github.tiagolofi.clients;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Message(
    @JsonProperty("message_id")
    Long messageId,
    @JsonProperty("chat_id")
    Long chatId,
    String text,
    Chat chat
) {}