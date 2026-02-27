package com.intercom.spring.ports.outbound;

import com.intercom.spring.domain.models.User;
import com.intercom.spring.domain.exception.ChatRepositoryException;

/**
 * Outbound (driven) port — defines what the domain needs for user persistence.
 * Implemented by infrastructure adapters (e.g. H2UserRepository).
 */
public interface UserRepository {
  User saveUser(String username, String email) throws ChatRepositoryException;
  boolean existsByUsername(String username) throws ChatRepositoryException;
  boolean existsByEmail(String email) throws ChatRepositoryException;
}

