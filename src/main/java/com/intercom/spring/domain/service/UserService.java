package com.intercom.spring.domain.service;

import com.intercom.spring.domain.exception.InvalidInputException;
import com.intercom.spring.domain.exception.UserRepositoryException;
import com.intercom.spring.domain.models.User;
import com.intercom.spring.ports.inbound.UserAPI;
import com.intercom.spring.ports.outbound.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Domain service — implements the inbound UserAPI port.
 * Contains user-related business rules.
 */
@Service
public class UserService implements UserAPI {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User signUp(String username, String email) throws UserRepositoryException, InvalidInputException {
        validateUsername(username);
        validateEmail(email);

        if (userRepository.existsByUsername(username)) {
            throw new InvalidInputException("Username '" + username + "' is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new InvalidInputException("Email '" + email + "' is already registered");
        }

        return userRepository.saveUser(username, email);
    }

    @Override
    public User login(String username) throws UserRepositoryException, InvalidInputException {
        if (username == null || username.isBlank()) {
            throw new InvalidInputException("Username cannot be empty");
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new InvalidInputException("User '" + username + "' not found");
        }
        return user;
    }

    @Override
    public List<User> searchUsers(String query) throws UserRepositoryException {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return userRepository.searchUsers(query);
    }

    private void validateUsername(String username) throws InvalidInputException {
        if (username == null || username.isBlank()) {
            throw new InvalidInputException("Username cannot be empty");
        }
        if (username.length() > 64) {
            throw new InvalidInputException("Username cannot exceed 64 characters");
        }
    }

    private void validateEmail(String email) throws InvalidInputException {
        if (email == null || email.isBlank()) {
            throw new InvalidInputException("Email cannot be empty");
        }
        if (!email.contains("@")) {
            throw new InvalidInputException("Email is not valid");
        }
        if (email.length() > 128) {
            throw new InvalidInputException("Email cannot exceed 128 characters");
        }
    }
}


