package com.intercom.spring.rest;

import com.intercom.spring.domain.models.User;
import com.intercom.spring.domain.exception.ChatRepositoryException;
import com.intercom.spring.domain.exception.InvalidInputException;
import com.intercom.spring.ports.inbound.UserAPI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    /**
     * Sign up a new user.
     * POST /api/users/signup?username=alice&email=alice@example.com
     */
    @PostMapping("/signup")
    public ResponseEntity<User> signUp(
            @RequestParam String username,
            @RequestParam String email) throws ChatRepositoryException, InvalidInputException {
        User user = userAPI.signUp(username, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}

