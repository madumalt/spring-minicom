package com.intercom.spring.ports.outbound;

import com.intercom.spring.domain.exception.UserRepositoryException;
import com.intercom.spring.domain.models.User;
import java.util.List;

/**
 * Outbound (driven) port — defines what the domain needs for user persistence.
 * Implemented by infrastructure adapters (e.g. H2UserRepository).
 */
public interface UserRepository {
  User saveUser(String username, String email) throws UserRepositoryException;
  boolean existsByUsername(String username) throws UserRepositoryException;
  boolean existsByEmail(String email) throws UserRepositoryException;
  User findByUsername(String username) throws UserRepositoryException;
  List<User> searchUsers(String query) throws UserRepositoryException;
}

