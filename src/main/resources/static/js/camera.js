document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('authToken');
    // DOM Elements
    const allViews = document.querySelectorAll('.view');
    const cameraStartView = document.getElementById('camera-start-view');
    const cameraActiveView = document.getElementById('camera-active-view');
    const imageCapturedView = document.getElementById('image-captured-view');
    const processingView = document.getElementById('processing-view');
    const resultDisplayView = document.getElementById('result-display-view');

    // Buttons
    const startCameraBtn = document.getElementById('start-camera-btn');
    const stopCameraBtn = document.getElementById('stop-camera-btn');
    const captureBtn = document.getElementById('capture-btn');
    const retakeBtn = document.getElementById('retake-btn');
    const processBtn = document.getElementById('process-btn');
    const copyBtn = document.getElementById('copy-btn');
    const clearBtn = document.getElementById('clear-btn');
    const exportWordBtn = document.getElementById('export-word-btn');
    const exportExcelBtn = document.getElementById('export-excel-btn');

    // Media Elements
    const video = document.getElementById('camera-video');
    const canvas = document.getElementById('camera-canvas');
    const capturedImagePreview = document.getElementById('captured-image-preview');
    const resultTextarea = document.getElementById('result-textarea');

    let stream = null;
    let capturedImageBlob = null;
    let extractedText = "";

    // --- View Management ---
    const showView = (viewToShow) => {
        allViews.forEach(v => v.style.display = 'none');
        viewToShow.style.display = 'block';
    };

    // **SỬA LỖI: Tạo hàm riêng để quản lý trạng thái xử lý/kết quả**
    const showProcessingState = (isProcessing) => {
        if (isProcessing) {
            if (processingView) processingView.style.display = 'flex';
            if (resultDisplayView) resultDisplayView.style.display = 'none';
        } else {
            if (processingView) processingView.style.display = 'none';
            if (resultDisplayView) resultDisplayView.style.display = 'block';
        }
    };

    // --- Camera Logic ---
    const startCamera = async () => {
        try {
            stream = await navigator.mediaDevices.getUserMedia({ video: true });
            video.srcObject = stream;
            showView(cameraActiveView);
        } catch (err) {
            alert("Không thể truy cập camera. Vui lòng cấp quyền.");
            console.error(err);
        }
    };

    const stopCamera = () => {
        if (stream) {
            stream.getTracks().forEach(track => track.stop());
            stream = null;
        }
    };

    const captureImage = () => {
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        canvas.getContext('2d').drawImage(video, 0, 0);
        canvas.toBlob(blob => {
            capturedImageBlob = blob;
            capturedImagePreview.src = URL.createObjectURL(blob);
            stopCamera();
            showView(imageCapturedView);
        }, 'image/jpeg');
    };

    // --- API Calls ---
    const handleProcessOCR = async () => {

        if (!token) {
            // Nếu không có token, người dùng chưa đăng nhập.
            alert('Vui lòng đăng nhập để sử dụng chức năng này.');
            // Chuyển ngay về trang đăng nhập.
            window.location.href = 'login.html';
            return; // Dừng thực thi tất cả các mã còn lại trong file này
        }

        if (isLimitReached()) {
            showLimitReachedModal();
            return;
        }
        if (!capturedImageBlob) {
            alert('Vui lòng chụp một bức ảnh trước.');
            return;
        }

        showView(processingView);
        showProcessingState(true); // **SỬA LỖI: Bật animation, ẩn text area**
        resultTextarea.value = 'Đang xử lý... Vui lòng chờ.';

        const formData = new FormData();
        // Chuyển Blob thành File để server nhận diện được tên file
        formData.append('files', new File([capturedImageBlob], "capture.jpg", { type: "image/jpeg" }));

        try {
            const token = localStorage.getItem('authToken');
            const headers = token ? { 'Authorization': `Bearer ${token}` } : {};

            const response = await fetch('/api/upload', {
                method: 'POST',
                headers: headers,
                body: formData,
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Lỗi không xác định từ server.');
            }
            const results = await response.json();
            if (results && results.length > 0) {
                extractedText = results[0].text;
                resultTextarea.value = extractedText;
                incrementUsageCount();
            } else {
                throw new Error("Không nhận được kết quả OCR.");
            }
        } catch (error) {
            resultTextarea.value = `Đã xảy ra lỗi: ${error.message}`;
        } finally {
            showProcessingState(false); // **SỬA LỖI: Tắt animation, hiện text area**
            showView(resultDisplayView);
        }
    };

    const handleExport = async (format) => {
        if (!extractedText) {
            alert("Không có nội dung để xuất file.");
            return;
        }
        try {
            const token = localStorage.getItem('authToken');
            const headers = {
                'Content-Type': 'application/json',
                ...(token && { 'Authorization': `Bearer ${token}` })
            };
            const response = await fetch(`/api/export?format=${format}`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ text: extractedText }),
            });

            if (!response.ok) throw new Error(`Lỗi khi xuất file ${format}.`);

            const blob = await response.blob();
            const header = response.headers.get('Content-Disposition');
            const filename = header ? header.split('filename=')[1].replace(/"/g, '') : `export.${format}`;

            saveAs(blob, filename);

        } catch (error) {
            alert(error.message);
        }
    };

    // --- Event Listeners ---
    startCameraBtn.addEventListener('click', startCamera);
    stopCameraBtn.addEventListener('click', () => {
        stopCamera();
        showView(cameraStartView);
    });
    captureBtn.addEventListener('click', captureImage);
    retakeBtn.addEventListener('click', startCamera);
    processBtn.addEventListener('click', handleProcessOCR);
    copyBtn.addEventListener('click', () => navigator.clipboard.writeText(resultTextarea.value).then(() => alert('Đã sao chép!')));
    clearBtn.addEventListener('click', () => {
        resultTextarea.value = '';
        capturedImageBlob = null;
        extractedText = '';
        showView(cameraStartView);
    });
    exportWordBtn.addEventListener('click', () => handleExport('docx'));
    exportExcelBtn.addEventListener('click', () => handleExport('xlsx'));
    // Hiển thị view ban đầu
    showView(cameraStartView);
});