package com.intercom.spring.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SendMessageRequest {
    private Long senderId;
    private String content;
}

