package org.project.backend.service;

import org.project.backend.model.User;
import org.project.backend.model.UserProfile;
import org.project.backend.payload.UserProfileDto;
import org.project.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Lấy thông tin chi tiết của người dùng đang đăng nhập.
     * @return UserProfileDto chứa thông tin của người dùng.
     */
    @Transactional(readOnly = true) // Đảm bảo chỉ đọc, tăng hiệu năng
    public UserProfileDto getCurrentUserProfile() {
        // 1. Lấy thông tin người dùng đã xác thực từ Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // 2. Tìm người dùng trong database bằng email
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + userEmail));

        // 3. Lấy thông tin profile liên quan
        UserProfile profile = user.getUserProfile();

        // 4. Chuyển đổi (Map) dữ liệu từ Entities sang DTO
        return convertToDto(user, profile);
    }

    /**
     * Phương thức trợ giúp để chuyển đổi User và UserProfile thành DTO.
     */
    private UserProfileDto convertToDto(User user, UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        // Gán tên gói cước hiện tại
        if (user.getPlan() != null) {
            dto.setCurrentPlan(user.getPlan().getName());
        }

        // Lấy thông tin từ profile nếu tồn tại
        if (profile != null) {
            dto.setFullName(profile.getFullName());
            dto.setGender(profile.getGender());
            dto.setAge(profile.getAge());
            dto.setPhoneNumber(profile.getPhoneNumber());
            dto.setJob(profile.getJob());
        }

        return dto;
    }
}