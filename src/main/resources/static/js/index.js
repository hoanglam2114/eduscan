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
    // KHỞI TẠO DỮ LIỆU NGƯỜI DÙNG VÀ LOGIC ĐĂNG NHẬP/XUẤT SIDEBAR
    // ===================================================

    const token = localStorage.getItem('authToken');

    // Mặc định cho Sidebar
    let userData = {
        fullName: 'Khách',
        email: 'Chưa đăng nhập',
        isAuthenticated: false
    };

    if (token) {
        const decodedToken = parseJwt(token);
        if (decodedToken) {
            userData.isAuthenticated = true;
            userData.fullName = decodedToken.sub || decodedToken.username || 'Người dùng';
            userData.email = decodedToken.email || '';
        }
    }

    // Initialize user info
    function initUserInfo() {
        // Sử dụng userData đã được cập nhật
        const initial = (userData.fullName || 'U').charAt(0).toUpperCase();

        const userNameEl = document.getElementById('userName');
        const sidebarAvatarEl = document.getElementById('sidebarAvatar');
        const sidebarNameEl = document.getElementById('sidebarName');
        const sidebarEmailEl = document.getElementById('sidebarEmail');
        const sidebarLogoutBtn = document.getElementById('logoutBtn');
        const sidebarLogoutText = document.getElementById('logout-text'); // Giả định bạn có một thẻ chứa text của nút

        if (userNameEl) userNameEl.textContent = userData.fullName;
        if (sidebarAvatarEl) sidebarAvatarEl.textContent = initial;
        if (sidebarNameEl) sidebarNameEl.textContent = userData.fullName;
        if (sidebarEmailEl) sidebarEmailEl.textContent = userData.email;

        // LOGIC NÚT ĐĂNG NHẬP/ĐĂNG XUẤT TRONG SIDEBAR
        if (sidebarLogoutBtn) {
            if (userData.isAuthenticated) {
                // Đã đăng nhập -> Đổi text thành Đăng xuất và gán logic Logout
                if (sidebarLogoutText) sidebarLogoutText.textContent = 'Đăng xuất';

                sidebarLogoutBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    if (confirm('Bạn có chắc muốn đăng xuất?')) {
                        localStorage.removeItem('authToken');
                        window.location.href = 'login.html'; // Điều hướng về trang login
                    }
                });
            } else {
                // Chưa đăng nhập -> Đổi text thành Đăng nhập và gán logic Login
                if (sidebarLogoutText) sidebarLogoutText.textContent = 'Đăng nhập';

                sidebarLogoutBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    // Điều hướng đến trang Đăng nhập
                    window.location.href = 'login.html';
                });
            }
        }
    }


    // ===================================================
    // LOGIC CỦA SIDEBAR VÀ CÁC NÚT KHÁC
    // ===================================================

    // Sidebar toggle
    const sidebar = document.getElementById('sidebar');
    const menuBtn = document.getElementById('menuBtn');
    const hamburgerBtn = document.getElementById('hamburgerBtn');
    const sidebarClose = document.getElementById('sidebarClose');

    function toggleSidebar() {
        if(sidebar) {
            sidebar.classList.toggle('closed');
            sidebar.classList.toggle('open');
        }
    }

    if(menuBtn) menuBtn.addEventListener('click', toggleSidebar);
    if(hamburgerBtn) hamburgerBtn.addEventListener('click', toggleSidebar);
    if(sidebarClose) sidebarClose.addEventListener('click', toggleSidebar);

    // Sidebar navigation
    const sidebarItems = document.querySelectorAll('.sidebar-item');
    sidebarItems.forEach(item => {
        item.addEventListener('click', () => {
            sidebarItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            if (window.innerWidth <= 480 && sidebar) {
                sidebar.classList.add('closed');
                sidebar.classList.remove('open');
            }
        });
    });

    // ===================================================
    // LOGIC ĐIỀU HƯỚNG CHO 3 BUTTON CHỨC NĂNG
    // ===================================================
    const actionButtons = document.querySelectorAll('.handwriting-ocr-btn, .camera-ocr-btn, .writing-assist-btn');

    actionButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            let targetUrl = '';

            if (btn.classList.contains('handwriting-ocr-btn')) {
                targetUrl = 'handwriting-ocr.html';
            } else if (btn.classList.contains('camera-ocr-btn')) {
                targetUrl = 'camera.html';
            } else if (btn.classList.contains('writing-assist-btn')) {
                targetUrl = 'WritingAssist.html';
            }

            if (targetUrl) {
                window.location.href = targetUrl;
            } else {
                const btnText = btn.querySelector('h3') ? btn.querySelector('h3').textContent : 'Action Button';
                console.error(`Error: Could not determine target URL for button: ${btnText}`);
            }
        });
    });

    // Khởi tạo các thành phần giao diện
    initUserInfo();
});