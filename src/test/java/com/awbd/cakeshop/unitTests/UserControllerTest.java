package com.awbd.cakeshop.unitTests;

import com.awbd.cakeshop.DTOs.LoginRequestDTO;
import com.awbd.cakeshop.DTOs.RegisterRequestDTO;
import com.awbd.cakeshop.DTOs.UserDTO;
import com.awbd.cakeshop.controllers.UserController;
import com.awbd.cakeshop.mappers.UserMapper;
import com.awbd.cakeshop.models.User;
import com.awbd.cakeshop.services.UserService;
import com.awbd.cakeshop.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserController userController;

    @Test
    void registerUser_ShouldReturnCreatedUser() {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO("alice", "Pass1234", "USER");

        User created = new User("alice", "Pass1234", User.Role.USER);
        created.setId(100L);

        UserDTO createdDTO = new UserDTO(100L, "alice", User.Role.USER);

        when(userService.create(any(User.class))).thenReturn(created);
        when(userMapper.toDto(any(User.class))).thenReturn(createdDTO);

        ResponseEntity<UserDTO> response = userController.registerUser(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("alice", response.getBody().getUsername());
        assertEquals(100L, response.getBody().getId());
    }

    @Test
    void login_ShouldReturnTokenAndUserDTO() {
        LoginRequestDTO login = new LoginRequestDTO("bob", "secure123");

        User user = new User("bob", Base64.getEncoder().encodeToString("secure123".getBytes()), User.Role.USER);
        user.setId(200L);

        UserDTO userDTO = new UserDTO(200L, "bob", User.Role.USER);

        when(userService.findByUsername("bob")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDTO);
        when(jwtUtil.generateToken("bob", User.Role.USER)).thenReturn("mock.jwt.token");

        ResponseEntity<Map<String, Object>> response = userController.login(login);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().containsKey("user"));
        assertTrue(response.getBody().containsKey("token"));
    }

    @Test
    void getUser_AsSameUser_ReturnsUserDTO() {
        long id = 1L;
        String token = "Bearer abc.def.ghi";

        User user = new User("carl", "pass", User.Role.USER);
        user.setId(1L);

        UserDTO dto = new UserDTO(1L, "carl", User.Role.USER);

        when(jwtUtil.getUsernameFromToken("abc.def.ghi")).thenReturn("carl");
        when(jwtUtil.getRoleFromToken("abc.def.ghi")).thenReturn(User.Role.USER);
        when(userService.findByUsername("carl")).thenReturn(Optional.of(user));
        when(userService.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        ResponseEntity<UserDTO> response = userController.getUser(id, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("carl", response.getBody().getUsername());
    }

    @Test
    void getUser_AsAdmin_ReturnsAnotherUserDTO() {
        long id = 2L;
        String token = "Bearer admin.jwt.token";

        User otherUser = new User("dave", "secret", User.Role.USER);
        otherUser.setId(2L);

        UserDTO dto = new UserDTO(2L, "dave", User.Role.USER);

        when(jwtUtil.getUsernameFromToken("admin.jwt.token")).thenReturn("admin");
        when(jwtUtil.getRoleFromToken("admin.jwt.token")).thenReturn(User.Role.ADMIN);
        when(userService.findById(id)).thenReturn(Optional.of(otherUser));
        when(userMapper.toDto(otherUser)).thenReturn(dto);

        ResponseEntity<UserDTO> response = userController.getUser(id, token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("dave", response.getBody().getUsername());
    }

    @Test
    void updateUser_SuccessfullyUpdatesUser() {
        long id = 3L;

        UserDTO input = new UserDTO(id, "emma", User.Role.USER);
        User toUpdate = new User("emma", "pass", User.Role.USER);
        toUpdate.setId(id);

        User updated = new User("emma", "pass", User.Role.USER);
        updated.setId(id);

        UserDTO updatedDTO = new UserDTO(id, "emma", User.Role.USER);

        when(userMapper.toEntity(input)).thenReturn(toUpdate);
        when(userService.update(id, toUpdate)).thenReturn(updated);
        when(userMapper.toDto(updated)).thenReturn(updatedDTO);

        ResponseEntity<UserDTO> response = userController.updateUser(id, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("emma", response.getBody().getUsername());
    }

    @Test
    void deleteUser_ShouldReturnOk() {
        long id = 4L;

        doNothing().when(userService).delete(id);

        ResponseEntity<Void> response = userController.deleteUser(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).delete(id);
    }
}
