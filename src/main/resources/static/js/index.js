// js/index.js

document.addEventListener('DOMContentLoaded', () => {

    /**
     * Hàm helper để giải mã payload của JWT.
     * @param {string} token - Chuỗi JWT.
     * @returns {object | null} - Dữ liệu payload của token hoặc null nếu token không hợp lệ.
     */
    function parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            console.error("Lỗi giải mã token:", e);
            return null;
        }
    }

    // ===================================================
    // LOGIC HIỂN THỊ ĐĂNG NHẬP / ĐĂNG XUẤT
    // ===================================================
    const userSection = document.getElementById('user-section');
    const token = localStorage.getItem('authToken');

    if (token) {
        // --- TRƯỜNG HỢP: ĐÃ ĐĂNG NHẬP ---

        const userData = parseJwt(token);
        const username = userData.sub || userData.username || 'Người dùng';

        // Tạo lời chào
        const greeting = document.createElement('span');
        greeting.textContent = `Chào, ${username}!`;

        // Tạo nút Đăng xuất
        const logoutButton = document.createElement('a');
        logoutButton.id = 'logout-button';
        logoutButton.className = 'logout-btn';
        logoutButton.textContent = 'Đăng xuất';
        logoutButton.href = 'login.html'; // Giữ nguyên thẻ <a> để dễ dàng style

        // Gán sự kiện click cho nút Đăng xuất
        logoutButton.addEventListener('click', () => {
            localStorage.removeItem('authToken');
            window.location.href = 'login.html';
        });

        // Thêm các phần tử vào div user-section
        userSection.appendChild(greeting);
        userSection.appendChild(logoutButton);

    } else {
        // --- TRƯỜNG HỢP: CHƯA ĐĂNG NHẬP ---

        // Tạo nút Đăng nhập
        const loginButton = document.createElement('a'); // Dùng thẻ <a> để dễ điều hướng
        loginButton.href = 'login.html';
        loginButton.className = 'logout-btn'; // Bạn có thể thêm class này vào CSS để style
        loginButton.textContent = 'Đăng nhập';

        // Thêm nút vào div user-section
        userSection.appendChild(loginButton);
    }

    // Các logic khác của trang chủ có thể đặt ở đây...
});