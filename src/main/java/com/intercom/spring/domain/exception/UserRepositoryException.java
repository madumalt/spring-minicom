package com.intercom.spring.domain.exception;

public class UserRepositoryException extends RuntimeException {

  public UserRepositoryException(String message) {
    super(message);
  }
}
