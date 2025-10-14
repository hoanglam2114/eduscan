package org.project.backend.controller;

import org.project.backend.payload.UserProfileDto;
import org.project.backend.repository.UserRepository;
import org.project.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

//    private final UserRepository userRepository;
//
//    // Inject repository qua constructor
//    public UserController(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    // ✅ Endpoint: Lấy thông tin user theo email (để xác định userId)
//    @GetMapping("/by-email")
//    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
//        try {
//            var user = userRepository.findByEmail(email);
//            if (user == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy user với email: " + email);
//            }
//            return ResponseEntity.ok(user);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Lỗi khi tìm user theo email: " + e.getMessage());
//        }
//    }

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * API Endpoint để lấy thông tin của người dùng hiện tại.
     * Chỉ những người dùng đã đăng nhập (có vai trò) mới có thể truy cập.
     * @return ResponseEntity chứa UserProfileDto.
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Đảm bảo chỉ người đã đăng nhập mới gọi được API này
    public ResponseEntity<UserProfileDto> getCurrentUser() {
        UserProfileDto userProfileDto = userService.getCurrentUserProfile();
        return ResponseEntity.ok(userProfileDto);
    }
}

