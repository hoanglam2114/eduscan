// js/usageLimiter.js

const USAGE_LIMIT = 5;
const STORAGE_KEY = 'anonymousUsageCount';

// Hàm lấy số lần đã sử dụng từ localStorage
function getUsageCount() {
    // Kiểm tra xem người dùng đã đăng nhập chưa (dựa vào authToken)
    if (localStorage.getItem('authToken')) {
        return 0; // Người dùng đã đăng nhập không bị giới hạn
    }
    const count = localStorage.getItem(STORAGE_KEY);
    return count ? parseInt(count, 10) : 0;
}

// Hàm tăng số lần sử dụng
function incrementUsageCount() {
    if (localStorage.getItem('authToken')) {
        return; // Không tăng cho người dùng đã đăng nhập
    }
    let currentCount = getUsageCount();
    localStorage.setItem(STORAGE_KEY, ++currentCount);
}

// Hàm kiểm tra xem đã hết lượt chưa
function isLimitReached() {
    return getUsageCount() >= USAGE_LIMIT;
}

// Hàm hiển thị thông báo khi hết lượt
function showLimitReachedModal() {
    // Bạn có thể thay thế alert bằng một modal đẹp hơn
    const userChoice = confirm(
        'Bạn đã hết 5 lần dùng thử miễn phí.\n\n' +
        'Vui lòng đăng nhập hoặc đăng ký để tiếp tục sử dụng không giới hạn.\n\n' +
        'Bạn có muốn đến trang đăng nhập không?'
    );

    if (userChoice) {
        window.location.href = 'login.html'; // Chuyển đến trang đăng nhập
    }
}