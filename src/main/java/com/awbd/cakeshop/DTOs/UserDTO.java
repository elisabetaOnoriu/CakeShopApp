package com.awbd.cakeshop.DTOs;

import com.awbd.cakeshop.models.User.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String password;
    private Role role;
    private List<Long> orderIds;
    private Long cartId;
    private List<Long> reviewIds;

    public UserDTO(Long id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }
}