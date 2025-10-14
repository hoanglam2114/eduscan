// js/user_profile.js

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
        const initial = (userData.fullName || 'U').charAt(0).toUpperCase();

        const userNameEl = document.getElementById('userName');
        const sidebarAvatarEl = document.getElementById('sidebarAvatar');
        const sidebarNameEl = document.getElementById('sidebarName');
        const sidebarEmailEl = document.getElementById('sidebarEmail');
        const sidebarLogoutBtn = document.getElementById('logoutBtn');
        const sidebarLogoutText = document.getElementById('logout-text');

        if (userNameEl) userNameEl.textContent = userData.fullName;
        if (sidebarAvatarEl) sidebarAvatarEl.textContent = initial;
        if (sidebarNameEl) sidebarNameEl.textContent = userData.fullName;
        if (sidebarEmailEl) sidebarEmailEl.textContent = userData.email;

        // LOGIC NÚT ĐĂNG NHẬP/ĐĂNG XUẤT TRONG SIDEBAR
        if (sidebarLogoutBtn) {
            if (userData.isAuthenticated) {
                if (sidebarLogoutText) sidebarLogoutText.textContent = 'Đăng xuất';

                sidebarLogoutBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    if (confirm('Bạn có chắc muốn đăng xuất?')) {
                        localStorage.removeItem('authToken');
                        window.location.href = 'login.html';
                    }
                });
            } else {
                if (sidebarLogoutText) sidebarLogoutText.textContent = 'Đăng nhập';

                sidebarLogoutBtn.addEventListener('click', (e) => {
                    e.preventDefault();
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

    // Khởi tạo các thành phần giao diện
    initUserInfo();

    // Load profile data
    loadUserProfile();
});

// ===================================================
// FETCH USER PROFILE FROM API
// ===================================================

async function loadUserProfile() {
    const loadingState = document.getElementById('loadingState');
    const profileSection = document.getElementById('profileSection');

    try {
        // Lấy token từ localStorage
        const token = localStorage.getItem('authToken');

        if (!token) {
            throw new Error('NO_TOKEN');
        }

        // Gọi API để lấy thông tin user
        const response = await fetch('/api/user/me', {
            method: 'GET',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            }
        });

        // Xử lý lỗi 401 Unauthorized
        if (response.status === 401) {
            throw new Error('UNAUTHORIZED');
        }

        // Xử lý các lỗi khác
        if (!response.ok) {
            throw new Error('API_ERROR');
        }

        // Parse JSON response
        const data = await response.json();

        // Cập nhật UI với dữ liệu user
        displayUserProfile(data);

        // Ẩn loading, hiện profile
        if (loadingState) loadingState.style.display = 'none';
        if (profileSection) profileSection.style.display = 'block';

    } catch (error) {
        console.error('Error loading profile:', error);

        let errorMessage = 'Không thể tải thông tin người dùng';
        let errorDescription = 'Vui lòng thử lại sau';
        let actionButton = `
            <button onclick="window.location.reload()" 
                style="margin-top: 16px; padding: 10px 20px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 600;">
                Tải lại
            </button>
        `;

        // Xử lý các loại lỗi cụ thể
        if (error.message === 'NO_TOKEN') {
            errorMessage = 'Chưa đăng nhập';
            errorDescription = 'Vui lòng đăng nhập để xem thông tin cá nhân';
            actionButton = `
                <button onclick="window.location.href='login.html'" 
                    style="margin-top: 16px; padding: 10px 20px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 600;">
                    Đăng nhập
                </button>
            `;
        } else if (error.message === 'UNAUTHORIZED') {
            errorMessage = 'Phiên đăng nhập đã hết hạn';
            errorDescription = 'Vui lòng đăng nhập lại để tiếp tục';
            actionButton = `
                <button onclick="localStorage.removeItem('authToken'); window.location.href='login.html'" 
                    style="margin-top: 16px; padding: 10px 20px; background: #667eea; color: white; border: none; border-radius: 8px; cursor: pointer; font-weight: 600;">
                    Đăng nhập lại
                </button>
            `;
        }

        // Hiển thị thông báo lỗi
        if (loadingState) {
            loadingState.innerHTML = `
                <div style="color: #ef4444; padding: 20px; text-align: center;">
                    <h3 style="margin-bottom: 8px;">❌ ${errorMessage}</h3>
                    <p style="color: #6b7280; margin-bottom: 16px;">${errorDescription}</p>
                    ${actionButton}
                </div>
            `;
        }
    }
}

// ===================================================
// DISPLAY USER PROFILE DATA
// ===================================================

function displayUserProfile(data) {
    // Hàm helper để cập nhật element an toàn
    const safeUpdate = (id, value) => {
        const element = document.getElementById(id);
        if (element) {
            element.textContent = value || '-';
        }
    };

    // Lấy chữ cái đầu tiên cho avatar
    const firstLetter = (data.fullName || data.username || '?').charAt(0).toUpperCase();

    // Cập nhật header
    safeUpdate('headerUserName', data.fullName || data.username);

    // Cập nhật sidebar
    safeUpdate('sidebarAvatar', firstLetter);
    safeUpdate('sidebarName', data.fullName || data.username);
    safeUpdate('sidebarEmail', data.email);

    // Cập nhật profile section
    safeUpdate('profileAvatar', firstLetter);
    safeUpdate('profileFullName', data.fullName || data.username);
    safeUpdate('profileEmail', data.email);

    // Cập nhật plan badge
    const planElement = document.getElementById('profilePlan');
    if (planElement) {
        planElement.textContent = data.currentPlan ?
            `${data.currentPlan.toUpperCase()} PLAN` : 'FREE PLAN';
    }

    // Cập nhật các trường thông tin
    safeUpdate('fieldUsername', data.username);
    safeUpdate('fieldEmail', data.email);

    updateField('fieldGender', data.gender);
    updateField('fieldAge', data.age ? `${data.age} tuổi` : null);
    updateField('fieldPhone', data.phoneNumber);
    updateField('fieldJob', data.job);
}

// ===================================================
// UPDATE FIELD WITH EMPTY STATE
// ===================================================

function updateField(fieldId, value) {
    const element = document.getElementById(fieldId);
    if (element) {
        if (value) {
            element.innerHTML = value;
        } else {
            element.innerHTML = '<span class="profile-field-empty">Chưa cập nhật</span>';
        }
    }
}

// ===================================================
// BUTTON ACTIONS
// ===================================================

// Button chỉnh sửa profile
const btnEditProfile = document.getElementById('btnEditProfile');
if (btnEditProfile) {
    btnEditProfile.addEventListener('click', () => {
        alert('Chức năng chỉnh sửa thông tin sẽ được triển khai sau!');
        // TODO: Chuyển đến trang edit hoặc hiển thị modal
        // window.location.href = 'edit_profile.html';
    });
}

// Button đổi mật khẩu
const btnChangePassword = document.getElementById('btnChangePassword');
if (btnChangePassword) {
    btnChangePassword.addEventListener('click', () => {
        alert('Chức năng đổi mật khẩu sẽ được triển khai sau!');
        // TODO: Chuyển đến trang change password hoặc hiển thị modal
        // window.location.href = 'change_password.html';
    });
}