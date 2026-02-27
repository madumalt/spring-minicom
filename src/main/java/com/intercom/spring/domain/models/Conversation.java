package com.intercom.spring.domain.models;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Conversation {
    private Long id;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}

