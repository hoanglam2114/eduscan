package org.project.backend.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UsageLimitingService {

    private final Cache<String, Integer> usageTracker;

    public UsageLimitingService() {
        // Tạo một cache để lưu số lần sử dụng của mỗi IP
        // Dữ liệu sẽ tự động bị xóa sau 24 giờ
        usageTracker = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
    }

    /**
     * Kiểm tra xem một IP có được phép thực hiện yêu cầu hay không.
     * @param ipAddress Địa chỉ IP của người dùng.
     * @return true nếu được phép, false nếu vượt quá giới hạn.
     */
    public boolean isAllowed(String ipAddress) {
        // Lấy số lần sử dụng hiện tại, nếu chưa có thì mặc định là 0
        int currentUsage = usageTracker.get(ipAddress, k -> 0);

        if (currentUsage >= 10) {
            return false; // Đã đạt giới hạn
        }

        // Tăng bộ đếm lên 1
        usageTracker.put(ipAddress, currentUsage + 1);
        return true;
    }

    /**
     * Lấy địa chỉ IP của client từ request.
     */
    public String getClientIp(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }
}