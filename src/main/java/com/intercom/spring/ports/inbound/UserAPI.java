package com.intercom.spring.ports.inbound;

import com.intercom.spring.domain.models.User;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import com.intercom.spring.domain.exception.InvalidInputException;

/**
 * Inbound (driving) port — defines use cases for user management.
 * Called by driving adapters (e.g. REST controller).
 * Implemented by the domain service.
 */
public interface UserAPI {
  User signUp(String username, String email) throws ChatRepositoryException, InvalidInputException;
}

