package org.project.backend.service;

import org.project.backend.model.Role;
import org.project.backend.model.SubscriptionPlan;
import org.project.backend.model.User;
import org.project.backend.payload.LoginDto;
import org.project.backend.payload.SignUpDto;
import org.project.backend.repository.RoleRepository;
import org.project.backend.repository.SubscriptionPlanRepository;
import org.project.backend.repository.UserRepository;
import org.project.backend.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       SubscriptionPlanRepository planRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.planRepository = planRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Xử lý logic đăng nhập
     */
    public String login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getEmail(), // Chúng ta dùng email làm username trong UserDetailsService
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }

    /**
     * Xử lý logic đăng ký
     */
    public String register(SignUpDto signUpDto) {
        // Kiểm tra username đã tồn tại chưa
        if (userRepository.existsByUsername(signUpDto.getUsername())) {
            // Ném ra một lỗi để Controller bắt được
            throw new RuntimeException("Username is already taken!");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(signUpDto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User user = new User();
        user.setUsername(signUpDto.getUsername());
        user.setEmail(signUpDto.getEmail());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword())); // Mã hóa mật khẩu

        // Gán vai trò mặc định là "customer"
        Role userRole = roleRepository.findByName("customer")
                .orElseThrow(() -> new RuntimeException("Error: Role 'customer' is not found."));
        user.setRoles(Collections.singleton(userRole));

        // Gán gói cước mặc định là "free"
        SubscriptionPlan freePlan = planRepository.findByName("free")
                .orElseThrow(() -> new RuntimeException("Error: Plan 'free' is not found."));
        user.setPlan(freePlan);

        userRepository.save(user);

        return "User registered successfully!";
    }
}