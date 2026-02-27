package com.intercom.spring.domain.models;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private Timestamp createdAt;
}
