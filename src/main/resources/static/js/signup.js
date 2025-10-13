// js/signup.js

document.addEventListener('DOMContentLoaded', () => {
    const signupForm = document.getElementById('signup-form');
    const usernameInput = document.getElementById('full-name-input');
    const emailInput = document.getElementById('email-input');
    const passwordInput = document.getElementById('password-input');
    const confirmPasswordInput = document.getElementById('confirm-password-input');
    const switchToLoginBtn = document.getElementById('switch-to-login');
    const errorMessageElement = document.getElementById('error-message');

    // API Endpoint của backend
    const API_URL = 'api/auth/signup';

    const handleSubmit = async (event) => {
        event.preventDefault();
        errorMessageElement.textContent = ''; // Xóa lỗi cũ

        const username = usernameInput.value;
        const email = emailInput.value;
        const password = passwordInput.value;
        const confirmPassword = confirmPasswordInput.value;

        // --- Kiểm tra phía client ---
        if (password !== confirmPassword) {
            errorMessageElement.textContent = 'Mật khẩu xác nhận không khớp!';
            return;
        }

        if (password.length < 6) {
            errorMessageElement.textContent = 'Mật khẩu phải có ít nhất 6 ký tự!';
            return;
        }

        // --- Gọi API Backend ---
        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, email, password }),
            });

            if (response.ok) {
                // Đăng ký thành công
                alert('Đăng ký thành công! Bạn sẽ được chuyển đến trang đăng nhập.');
                window.location.href = '../login.html'; // Chuyển trang
            } else {
                // Xử lý lỗi từ server (ví dụ: email đã tồn tại)
                const errorText = await response.text();
                errorMessageElement.textContent = errorText;
            }
        } catch (error) {
            // Xử lý lỗi mạng
            console.error('Lỗi khi gọi API:', error);
            errorMessageElement.textContent = 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.';
        }
    };

    // Chuyển sang trang đăng nhập
    const handleSwitchToLogin = () => {
        window.location.href = '../login.html';
    };

    signupForm.addEventListener('submit', handleSubmit);
    switchToLoginBtn.addEventListener('click', handleSwitchToLogin);
});