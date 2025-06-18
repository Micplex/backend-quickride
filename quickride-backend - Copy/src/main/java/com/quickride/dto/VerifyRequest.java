package com.quickride.dto;

import lombok.Data;

@Data
public class VerifyRequest {
    private String email;
    private String verificationCode;
} 