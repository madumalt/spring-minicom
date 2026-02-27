package com.intercom.spring.domain.models;

import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
  private Long id;
  private String username;
  private String email;
  private String status;
  private Timestamp createdAt;
  private Timestamp updatedAt;
}
