package com.intercom.spring.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SendMessageRequest {
    @JsonProperty("sender_id")
    private Long senderId;
    private String content;
}

