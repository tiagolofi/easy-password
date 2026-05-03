package com.github.tiagolofi.clients;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Chat(
    Long id,
    @JsonProperty("first_name")
    String firstName
) {}
