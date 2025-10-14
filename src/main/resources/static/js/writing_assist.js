// Template selection
const templates = document.querySelectorAll('.wa-template');
const promptInput = document.getElementById('promptInput');

templates.forEach(template => {
    template.addEventListener('click', () => {
        templates.forEach(t => t.classList.remove('selected'));
        template.classList.add('selected');
        // Đảm bảo promptInput nhận giá trị từ data-prompt
        promptInput.value = template.dataset.prompt;
        promptInput.focus();
    });
});

// Các phần tử giao diện
const generateBtn = document.getElementById('generateBtn');
const generateBtnText = document.getElementById('generateBtnText');
const resultContainer = document.getElementById('resultContainer');
const processingView = document.getElementById('processingView');
const resultDisplayView = document.getElementById('resultDisplayView');
const resultText = document.getElementById('resultText');
const usageCount = document.getElementById('usageCount');
const copyBtn = document.getElementById('copyBtn');
const downloadBtn = document.getElementById('downloadBtn');

// Hàm cập nhật trạng thái UI khi đang xử lý
function setProcessing(isProcessing) {
    if (isProcessing) {
        resultContainer.style.display = 'block';
        processingView.style.display = 'block';
        resultDisplayView.style.display = 'none';
        generateBtn.disabled = true;
        generateBtnText.textContent = 'Đang xử lý...';
    } else {
        processingView.style.display = 'none';
        // Hiển thị khung kết quả chỉ khi có nội dung hoặc lỗi
        if (resultText.value.trim() !== '') {
            resultDisplayView.style.display = 'block';
        } else {
            resultDisplayView.style.display = 'none';
        }
        generateBtn.disabled = false;
        generateBtnText.textContent = 'Tạo nội dung';
    }
}

// ====================================================================
// 1. Logic Call API Generate Content (Sửa lỗi 401)
// ====================================================================
const token = localStorage.getItem('authToken');

generateBtn.addEventListener('click', async () => {
    if (!token) {
        // Nếu không có token, người dùng chưa đăng nhập.
        alert('Vui lòng đăng nhập để sử dụng chức năng này.');
        // Chuyển ngay về trang đăng nhập.
        window.location.href = 'login.html';
        return; // Dừng thực thi tất cả các mã còn lại trong file này
    }
    const prompt = promptInput.value.trim();
    if (!prompt) {
        alert('Vui lòng nhập yêu cầu hoặc chọn mẫu!');
        return;
    }

    // Khởi tạo trạng thái xử lý
    setProcessing(true);
    resultText.value = ''; // Xóa kết quả cũ
// --- PHẦN CẢI TIẾN PROMPT ---
    // Đây là "kim chỉ nam" cho AI. Bạn có thể tùy chỉnh câu chữ ở đây.
    const promptPrefix = "Hãy đóng vai một trợ lý AI hữu ích. Trả lời trực tiếp, tự nhiên và đi thẳng vào vấn đề người dùng yêu cầu. Không thêm các câu giới thiệu hay kết luận không cần thiết. Yêu cầu của người dùng là: ";

    const finalPrompt = promptPrefix + prompt;
    // ----------------------------
    try {
        const response = await fetch('/api/assist/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // NẾU CẦN: Thêm header Authorization (ví dụ: JWT token) vào đây
                // 'Authorization': 'Bearer ' + localStorage.getItem('jwtToken'),
            },
            body: JSON.stringify({ prompt: finalPrompt })
        });
        // In ra để kiểm tra prompt cuối cùng trông như thế nào
        console.log("Final Prompt Sent to Backend:", finalPrompt);

        // Kiểm tra phản hồi HTTP
        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = `Lỗi HTTP ${response.status}`;

            if (response.status === 401) {
                errorMessage = "Lỗi xác thực (401 Unauthorized): Yêu cầu cần đăng nhập hoặc thiếu token hợp lệ.";
            } else if (errorText.trim() !== '') {
                errorMessage += `: ${errorText}`;
            }

            throw new Error(errorMessage);
        }

        // Backend trả về text (String) trực tiếp, không phải JSON
        const generatedText = await response.text();

        // Cập nhật kết quả vào textarea
        resultText.value = generatedText;

        // Cập nhật số lần sử dụng (Logic giả lập)
        let count = parseInt(usageCount.textContent) || 0;
        // Giả định giới hạn là 5 lần, nếu có hệ thống user/session thì cần lấy từ backend
        usageCount.textContent = Math.min(count + 1, 5);

    } catch (error) {
        console.error("Lỗi khi gọi API Writing Assistance:", error);
        resultText.value = `❌ Đã xảy ra lỗi: ${error.message}. Vui lòng kiểm tra console hoặc liên hệ hỗ trợ.`;
    } finally {
        // Kết thúc trạng thái xử lý
        setProcessing(false);
        // Cuộn đến phần kết quả
        resultContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
});

// ====================================================================
// 2. Logic Copy và Export (Mặc định file Word - docx)
// ====================================================================

// Clear button
document.getElementById('clearBtn').addEventListener('click', () => {
    promptInput.value = '';
    resultText.value = '';
    resultContainer.style.display = 'none';
    templates.forEach(t => t.classList.remove('selected'));
});

// Copy button
copyBtn.addEventListener('click', () => {
    if (resultText.value) {
        resultText.select();
        // Fallback cho document.execCommand (bị deprecated)
        try {
            document.execCommand('copy');
        } catch (err) {
            navigator.clipboard.writeText(resultText.value);
        }
        alert('Đã sao chép nội dung!');
    }
});

// Download button (Mặc định là file word và loại bỏ hộp thoại prompt)
downloadBtn.addEventListener('click', async () => {
    const contentToExport = resultText.value.trim();
    if (!contentToExport) {
        alert('Không có nội dung để tải xuống.');
        return;
    }

    // Đặt mặc định định dạng file là 'word' theo yêu cầu mới
    const format = 'docx';

    // Dữ liệu gửi đi phải tuân thủ model OcrResult { text: String }
    const payload = {
        text: contentToExport
    };

    try {
        // Call API Export ở WritingAssistantController
        const response = await fetch(`/api/assist/export?format=${format}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorBody = await response.text();
            throw new Error(`Lỗi Export: Không thể tạo file ${format.toUpperCase()}. (${response.status})`);
        }

        // Lấy tên file từ header Content-Disposition
        const disposition = response.headers.get('Content-Disposition');
        let filename = `writing-result.${format === 'word' ? 'docx' : format}`; // docx nếu là word
        if (disposition && disposition.indexOf('attachment') !== -1) {
            // Regex để trích xuất tên file
            const filenameMatch = disposition.match(/filename="?([^"]*)"?/i);
            if (filenameMatch && filenameMatch[1]) {
                filename = filenameMatch[1];
            }
        }

        // Lấy dữ liệu file (Blob)
        const blob = await response.blob();

        // Tạo liên kết và kích hoạt tải xuống
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        // alert(`Đã tải xuống file "${filename}"`); // Có thể thay bằng thông báo đẹp hơn

    } catch (error) {
        console.error("Lỗi khi tải xuống:", error);
        alert(`Lỗi trong quá trình tải xuống: ${error.message}`);
    }
});

// Upgrade button (Giữ nguyên logic cũ nếu có)
document.getElementById('upgradeBtn').addEventListener('click', () => {
    alert('Chuyển đến trang nâng cấp dịch vụ!');
});