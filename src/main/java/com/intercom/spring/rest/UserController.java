package com.intercom.spring.rest;

import com.intercom.spring.domain.models.User;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import com.intercom.spring.domain.exception.InvalidInputException;
import com.intercom.spring.ports.inbound.UserAPI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Driving (inbound) adapter — REST API for user management.
 * Depends only on the inbound UserAPI port.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserAPI userAPI;

    public UserController(UserAPI userAPI) {
        this.userAPI = userAPI;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signUp(
            @RequestParam String username,
            @RequestParam String email) throws ChatRepositoryException, InvalidInputException {
        User user = userAPI.signUp(username, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(
            @RequestParam String username) throws ChatRepositoryException, InvalidInputException {
        User user = userAPI.login(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam String query) throws ChatRepositoryException {
        List<User> users = userAPI.searchUsers(query);
        return ResponseEntity.ok(users);
    }
}

