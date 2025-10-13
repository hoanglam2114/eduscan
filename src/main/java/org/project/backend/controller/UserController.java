package org.project.backend.controller;

import org.project.backend.repository.UserRepository;
import org.project.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // Inject repository qua constructor
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Endpoint: Lấy thông tin user theo email (để xác định userId)
    @GetMapping("/by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            var user = userRepository.findByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy user với email: " + email);
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm user theo email: " + e.getMessage());
        }
    }
}

