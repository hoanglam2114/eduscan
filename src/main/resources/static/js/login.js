// js/login.js

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const signupBtn = document.getElementById('signupBtn');
    const errorMessageElement = document.getElementById('error-message');

    // API Endpoint của backend
    const API_URL = 'http://localhost:8080/api/auth/login';

    const handleSubmit = async (event) => {
        event.preventDefault();
        errorMessageElement.textContent = ''; // Xóa lỗi cũ

        const email = emailInput.value;
        const password = passwordInput.value;

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
            });

            if (response.ok) {
                const data = await response.json();
                // Lưu token vào localStorage để sử dụng cho các API cần xác thực sau này
                localStorage.setItem('authToken', data.accessToken);

                alert('Đăng nhập thành công!');

                // Chuyển hướng đến trang chính của ứng dụng (ví dụ: dashboard.html)
                // window.location.href = '../html/dashboard.html';
                window.location.href = 'index.html';
            } else {
                // Sai email hoặc mật khẩu
                errorMessageElement.textContent = 'Thông tin đăng nhập không chính xác.';
            }
        } catch (error) {
            console.error('Lỗi khi gọi API:', error);
            errorMessageElement.textContent = 'Không thể kết nối đến máy chủ. Vui lòng thử lại sau.';
        }
    };

    // Chuyển sang trang đăng ký
    const handleRedirectToSignup = () => {
        window.location.href = '../signup.html';
    };

    loginForm.addEventListener('submit', handleSubmit);
    signupBtn.addEventListener('click', handleRedirectToSignup);


    // Forgot password handler
    document.getElementById('forgotPasswordBtn').addEventListener('click', function() {
        window.location.href = '../forgot-password.html'; // Adjust the path as necessary
        // alert('Forgot password feature will be implemented');
    });
});