
// js/index.js

document.addEventListener('DOMContentLoaded', () => {

    // ===================================================
    // 1. AUTHENTICATION GUARD (Bảo vệ trang)
    // ===================================================
    const token = localStorage.getItem('authToken');

    // if (!token) {
    //     // Nếu không có token, người dùng chưa đăng nhập.
    //     // Chuyển ngay về trang đăng nhập.
    //     alert('Vui lòng đăng nhập để tiếp tục.');
    //     window.location.href = 'login.html';
    //     return; // Dừng thực thi các mã còn lại
    // }

    // ===================================================
    // 2. LOGOUT LOGIC (Hoàn thiện chức năng đăng xuất)
    // ===================================================
    const logoutButton = document.getElementById('logout-button');

    if (logoutButton) {
        logoutButton.addEventListener('click', () => {
            // Xóa token khỏi localStorage
            localStorage.removeItem('authToken');

            // alert('Bạn đã đăng xuất thành công.');

            // Điều hướng về trang đăng nhập
            window.location.href = 'login.html';
        });
    }

    // Bạn có thể thêm các mã khác cho trang chủ ở đây
    // Ví dụ: Lấy thông tin người dùng từ token và hiển thị tên, v.v.
});