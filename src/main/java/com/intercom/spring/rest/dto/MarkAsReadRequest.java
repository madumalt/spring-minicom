package com.intercom.spring.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarkAsReadRequest {
    @JsonProperty("user_id")
    private Long userId;
}

