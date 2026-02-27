package com.intercom.spring.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarkAsReadRequest {
    private Long userId;
}

