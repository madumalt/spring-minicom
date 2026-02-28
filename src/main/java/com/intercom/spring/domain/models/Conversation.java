package com.intercom.spring.domain.models;

import java.sql.Timestamp;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Conversation {
    private Long id;
    private String type; // "private" or "group"
    private String name; // null for private, user-defined for group
    private List<String> participantUsernames;
    private int unreadCount;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

