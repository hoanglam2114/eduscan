package org.project.backend.controller;

import org.project.backend.payload.JwtAuthResponse;
import org.project.backend.payload.LoginDto;
import org.project.backend.payload.SignUpDto;
import org.project.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * API Endpoint cho việc đăng nhập
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        try {
            String token = authService.login(loginDto);
            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse(token);
            return ResponseEntity.ok(jwtAuthResponse);
        } catch (Exception e) {
            // Trả về lỗi 401 nếu sai thông tin đăng nhập
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * API Endpoint cho việc đăng ký
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<String> register(@RequestBody SignUpDto signUpDto) {
        try {
            String responseMessage = authService.register(signUpDto);
            // Trả về 201 Created nếu thành công
            return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Trả về 400 Bad Request nếu username/email đã tồn tại
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}