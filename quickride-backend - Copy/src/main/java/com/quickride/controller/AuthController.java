package com.quickride.controller;

import com.quickride.dto.*;
import com.quickride.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("Signup successful. Verification code sent.");
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {
        authService.verify(request);
        return ResponseEntity.ok("Verification successful.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody PasswordForgotRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Password reset code sent.");
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request, @RequestParam String code) {
        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setEmail(request.getEmail());
        verifyRequest.setVerificationCode(code);
        authService.resetPassword(verifyRequest, request.getNewPassword());
        return ResponseEntity.ok("Password reset successful.");
    }
} 