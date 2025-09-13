package com.awbd.cakeshop.controllers;

import com.awbd.cakeshop.DTOs.LoginRequestDTO;
import com.awbd.cakeshop.DTOs.RegisterRequestDTO;
import com.awbd.cakeshop.DTOs.UserDTO;
import com.awbd.cakeshop.exceptions.user.InvalidRoleException;
import com.awbd.cakeshop.mappers.UserMapper;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.services.UserService;
import com.awbd.cakeshop.utils.JwtUtil;
import com.awbd.cakeshop.utils.TokenBlacklistService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    public UserController(UserService userService,
                          UserMapper userMapper,
                          JwtUtil jwtUtil,
                          PasswordEncoder passwordEncoder,
                          TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    // ---------- Helpers ----------
    private User.Role parseRoleOrThrow(String incomingRole) {
        if (incomingRole == null || incomingRole.isBlank()) {
            return User.Role.USER; // default
        }
        try {
            return User.Role.valueOf(incomingRole.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRoleException("Role must be ADMIN or USER");
        }
    }

    // ---------- Endpoints ----------

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername().trim());
        newUser.setPassword(registerRequest.getPassword());
        newUser.setRole(parseRoleOrThrow(registerRequest.getRole()));

        User createdUser = userService.create(newUser);

        return ResponseEntity
                .created(URI.create("/api/users/" + createdUser.getId()))
                .body(userMapper.toDto(createdUser));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        String username = Optional.ofNullable(loginRequest.getUsername()).orElse("").trim();
        String rawPassword = Optional.ofNullable(loginRequest.getPassword()).orElse("");

        return userService.findByUsername(username)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getUsername(), u.getRole());
                    Map<String, Object> body = new HashMap<>();
                    body.put("user", userMapper.toDto(u));
                    body.put("token", token);
                    body.put("tokenType", "Bearer");
                    return ResponseEntity.ok(body);
                })
                .orElseGet(() -> {
                    Map<String, Object> err = Map.of("error", "Invalid credentials");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
                });
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            tokenBlacklistService.invalidateToken(token);
            log.debug("Token invalidated via logout");
        }
        return ResponseEntity.ok().build();
    }

    // (opțional) endpoint util de sanity-check pentru token
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Missing bearer token"));
        }
        String token = authHeader.substring(7).trim();
        if (!jwtUtil.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired token"));
        }
        String username = jwtUtil.getUsernameFromToken(token);
        var role = jwtUtil.getRoleFromToken(token);
        return ResponseEntity.ok(Map.of("username", username, "role", role != null ? role.name() : null));
    }
}
