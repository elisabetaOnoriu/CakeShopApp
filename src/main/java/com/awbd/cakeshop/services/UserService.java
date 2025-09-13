package com.awbd.cakeshop.services;

import com.awbd.cakeshop.exceptions.user.*;
import com.awbd.cakeshop.models.Cart;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.repositories.CartRepository;
import com.awbd.cakeshop.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MIN_PASSWORD_LENGTH = 8;

    private static final Pattern BCRYPT_PATTERN =
            Pattern.compile("^\\$2[aby]?\\$\\d{2}\\$[A-Za-z0-9./]{53}$");

    public UserService(UserRepository userRepository,
                       CartRepository cartRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isPasswordStrong(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    private boolean isUsernameValid(String username) {
        return username != null && !username.trim().isEmpty();
    }

    private boolean isBCryptHash(String value) {
        return value != null && BCRYPT_PATTERN.matcher(value).matches();
    }

    private void validateAdminCreation() {
        long adminCount = userRepository.countByRole(User.Role.ADMIN);
        if (adminCount >= 3) {
            throw new AdminLimitExceededException("Cannot create more than 3 admin users.");
        }
    }

    public User create(User user) {
        if (!isUsernameValid(user.getUsername())) {
            throw new InvalidUsernameException("Username cannot be empty");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUserException("Username already exists");
        }
        if (!isPasswordStrong(user.getPassword())) {
            throw new WeakPasswordException("Password must be at least 8 characters long");
        }
        if (user.getRole() == null || (user.getRole() != User.Role.ADMIN && user.getRole() != User.Role.USER)) {
            throw new InvalidRoleException("Role must be either ADMIN or USER");
        }
        if (user.getRole() == User.Role.ADMIN) {
            validateAdminCreation();
        }

        String rawOrHash = user.getPassword();
        user.setPassword(isBCryptHash(rawOrHash) ? rawOrHash : passwordEncoder.encode(rawOrHash));

        try {
            User savedUser = userRepository.save(user);

            Cart cart = new Cart();
            cart.setUser(savedUser);
            cart.setCakes(new HashSet<>());
            cartRepository.save(cart);

            savedUser.setCart(cart);
            return savedUser;

        } catch (DataIntegrityViolationException e) {
            throw new UserCreationException("Failed to create user due to data integrity violation", e);
        }
    }

    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    public Optional<User> findByUsername(String username) { return userRepository.findByUsername(username); }

    public User update(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    String newUsername = updatedUser.getUsername();
                    if (!existingUser.getUsername().equals(newUsername) &&
                            userRepository.existsByUsername(newUsername)) {
                        throw new DuplicateUserException("Username already exists");
                    }
                    if (!isUsernameValid(newUsername)) {
                        throw new InvalidUsernameException("Username cannot be empty");
                    }
                    existingUser.setUsername(newUsername);

                    String newPassword = updatedUser.getPassword();
                    if (newPassword != null && !newPassword.isBlank()) {
                        if (!isPasswordStrong(newPassword) && !isBCryptHash(newPassword)) {
                            throw new WeakPasswordException("Password must be at least 8 characters long");
                        }

                        existingUser.setPassword(isBCryptHash(newPassword) ? newPassword : passwordEncoder.encode(newPassword));
                    }

                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found."));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id " + id + " not found.");
        }
        userRepository.deleteById(id);
    }


    public void changePassword(Long userId, String oldRawPassword, String newRawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " not found."));

        if (oldRawPassword == null || newRawPassword == null) {
            throw new WeakPasswordException("Password cannot be null");
        }
        if (!passwordEncoder.matches(oldRawPassword, user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }
        if (!isPasswordStrong(newRawPassword)) {
            throw new WeakPasswordException("Password must be at least 8 characters long");
        }

        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }
}
