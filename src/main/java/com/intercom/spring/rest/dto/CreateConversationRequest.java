package com.intercom.spring.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateConversationRequest {
    private String type;
    private String name;

    @JsonProperty("participant_ids")
    private List<Long> participantIds;
}

