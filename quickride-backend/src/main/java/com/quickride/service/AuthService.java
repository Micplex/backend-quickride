package com.quickride.service;

import com.quickride.dto.*;
import com.quickride.model.User;
import com.quickride.repository.UserRepository; // <-- Add this import!
import com.quickride.security.JwtTokenProvider;
import com.quickride.util.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already in use");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        String verificationCode = OtpUtil.generateOtp();
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encodedPassword)
                .phone(request.getPhone())
                .role(request.getRole())
                .isVerified(false)
                .verificationCode(verificationCode)
                .build();
        userRepository.save(user);
        // Send verification via email and SMS
        emailService.sendEmail(user.getEmail(), "QuickRide Verification Code", "Your verification code is: " + verificationCode);
        if (user.getPhone() != null) {
            smsService.sendSms(user.getPhone(), "Your QuickRide verification code is: " + verificationCode);
        }
    }

    @Transactional
    public void verify(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.isVerified()) {
            throw new RuntimeException("User already verified");
        }
        if (!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }
        user.setVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);
    }

    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isVerified()) {
            throw new RuntimeException("User not verified");
        }
        return jwtTokenProvider.generateToken(user);
    }

    public void forgotPassword(PasswordForgotRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String otp = OtpUtil.generateOtp();
        user.setVerificationCode(otp);
        userRepository.save(user);
        emailService.sendEmail(user.getEmail(), "QuickRide Password Reset Code", "Your password reset code is: " + otp);
        if (user.getPhone() != null) {
            smsService.sendSms(user.getPhone(), "Your QuickRide password reset code is: " + otp);
        }
    }

    @Transactional
    public void resetPassword(VerifyRequest request, String newPassword) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new RuntimeException("Invalid verification code");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setVerificationCode(null);
        userRepository.save(user);

    }
}