package com.quickride.dto;

import com.quickride.model.User;
import lombok.Data;

@Data
public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private User.Role role;
} 