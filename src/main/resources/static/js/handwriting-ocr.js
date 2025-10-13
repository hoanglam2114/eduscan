document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements - Gán vào các biến
    const uploadView = document.getElementById('upload-view');
    const previewView = document.getElementById('preview-view');
    const uploadArea = document.getElementById('upload-area');
    const fileInput = document.getElementById('file-input');
    const browseButton = document.getElementById('browse-button');
    const imagePreview = document.getElementById('image-preview');
    const processButton = document.getElementById('process-button');
    const clearButton = document.getElementById('clear-button');

    // SỬA LỖI: ID đúng trong HTML là 'result-section'
    const resultSection = document.getElementById('result-section');

    const processingView = document.getElementById('processing-view');
    const resultDisplayView = document.getElementById('result-display-view');
    const resultTextarea = document.getElementById('result-textarea');
    const copyButton = document.getElementById('copy-button');
    const exportWordButton = document.getElementById('export-word-button');
    const exportExcelButton = document.getElementById('export-excel-button');

    let selectedFile = null;
    let extractedText = "";

    // --- View Management ---
    const showView = (viewToShow) => {
        if (uploadView) uploadView.style.display = 'none';
        if (previewView) previewView.style.display = 'none';
        if (viewToShow) viewToShow.style.display = 'block';
    };

    const showProcessing = (isProcessing) => {
        if (processingView) processingView.style.display = isProcessing ? 'flex' : 'none';
        if (resultDisplayView) resultDisplayView.style.display = isProcessing ? 'none' : 'block';
    };

    // --- File Handling ---
    const handleFileSelect = (file) => {
        if (file && file.type.startsWith('image/')) {
            selectedFile = file;
            const reader = new FileReader();
            reader.onload = (e) => {
                if (imagePreview) imagePreview.src = e.target.result;
                showView(previewView);
                if (resultSection) resultSection.style.display = 'none';
            };
            reader.readAsDataURL(file);
        } else {
            alert('Vui lòng chọn một file ảnh hợp lệ.');
        }
    };

    // --- Event Listeners ---
    // Thêm kiểm tra 'null' để code an toàn hơn
    if (browseButton) browseButton.addEventListener('click', () => fileInput && fileInput.click());
    if (fileInput) fileInput.addEventListener('change', (e) => handleFileSelect(e.target.files[0]));
    if (uploadArea) {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            uploadArea.addEventListener(eventName, e => { e.preventDefault(); e.stopPropagation(); }, false);
        });
        uploadArea.addEventListener('drop', (e) => handleFileSelect(e.dataTransfer.files[0]));
    }

    if (clearButton) clearButton.addEventListener('click', () => {
        selectedFile = null;
        if (fileInput) fileInput.value = '';
        if (imagePreview) imagePreview.src = '';
        showView(uploadView);
        if (resultSection) resultSection.style.display = 'none';
        if (resultTextarea) resultTextarea.value = '';
        extractedText = '';
    });

    // --- API Calls ---
    if (processButton) processButton.addEventListener('click', async () => {
        // Kiểm tra sự tồn tại của hàm trước khi gọi
        if (typeof isLimitReached === 'function' && isLimitReached()) {
            showLimitReachedModal();
            return;
        }
        if (!selectedFile) {
            alert('Vui lòng chọn một file ảnh.');
            return;
        }

        if (resultSection) resultSection.style.display = 'block';
        showProcessing(true);

        const formData = new FormData();
        formData.append('files', selectedFile);

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
                if (resultTextarea) resultTextarea.value = extractedText;
                if (typeof incrementUsageCount === 'function') incrementUsageCount();
            } else {
                throw new Error("Không nhận được kết quả OCR.");
            }

        } catch (error) {
            if (resultTextarea) resultTextarea.value = `Đã xảy ra lỗi: ${error.message}`;
        } finally {
            showProcessing(false);
        }
    });

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

            // Kiểm tra sự tồn tại của hàm saveAs
            if (typeof saveAs === 'function') {
                saveAs(blob, filename);
            } else {
                alert('Thư viện FileSaver.js chưa được tải. Không thể tải file.');
            }
        } catch (error) {
            alert(error.message);
        }
    };

    if (copyButton) copyButton.addEventListener('click', () => {
        if(resultTextarea) navigator.clipboard.writeText(resultTextarea.value).then(() => alert('Đã sao chép!'))
    });
    if (exportWordButton) exportWordButton.addEventListener('click', () => handleExport('docx'));
    if (exportExcelButton) exportExcelButton.addEventListener('click', () => handleExport('xlsx'));
});