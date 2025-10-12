document.addEventListener('DOMContentLoaded', () => {
    // DOM Elements
    const uploadView = document.getElementById('upload-view');
    const previewView = document.getElementById('preview-view');
    const uploadArea = document.getElementById('upload-area');
    const fileInput = document.getElementById('file-input');
    const browseButton = document.getElementById('browse-button');
    const imagePreview = document.getElementById('image-preview');

    const processButton = document.getElementById('process-button');
    const clearButton = document.getElementById('clear-button');

    const resultsSection = document.getElementById('results-section');
    const processingView = document.getElementById('processing-view');
    const resultDisplayView = document.getElementById('result-display-view');
    const resultTextarea = document.getElementById('result-textarea');

    const copyButton = document.getElementById('copy-button');
    const exportWordButton = document.getElementById('export-word-button');
    const exportExcelButton = document.getElementById('export-excel-button');

    let selectedFile = null;
    let extractedText = ""; // Biến lưu kết quả OCR

    // --- View Management ---
    const showView = (viewToShow) => {
        [uploadView, previewView].forEach(v => v.style.display = 'none');
        viewToShow.style.display = 'block';
    };

    const showProcessing = (isProcessing) => {
        processingView.style.display = isProcessing ? 'flex' : 'none';
        resultDisplayView.style.display = isProcessing ? 'none' : 'block';
    };

    // --- File Handling ---
    const handleFileSelect = (file) => {
        if (file && file.type.startsWith('image/')) {
            selectedFile = file;
            const reader = new FileReader();
            reader.onload = (e) => {
                imagePreview.src = e.target.result;
                showView(previewView);
                resultsSection.style.display = 'none';
            };
            reader.readAsDataURL(file);
        } else {
            alert('Vui lòng chọn một file ảnh hợp lệ.');
        }
    };

    // --- Event Listeners ---
    browseButton.addEventListener('click', () => fileInput.click());
    fileInput.addEventListener('change', (e) => handleFileSelect(e.target.files[0]));
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
        uploadArea.addEventListener(eventName, e => {
            e.preventDefault();
            e.stopPropagation();
        }, false);
    });
    uploadArea.addEventListener('drop', (e) => handleFileSelect(e.dataTransfer.files[0]));

    clearButton.addEventListener('click', () => {
        selectedFile = null;
        fileInput.value = '';
        imagePreview.src = '';
        showView(uploadView);
        resultsSection.style.display = 'none';
        resultTextarea.value = '';
        extractedText = '';
    });

    // --- API Calls ---
    processButton.addEventListener('click', async () => {
        if (isLimitReached()) {
            showLimitReachedModal();
            return;
        }

        if (!selectedFile) {
            alert('Vui lòng chọn một file ảnh.');
            return;
        }

        resultsSection.style.display = 'block';
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
                extractedText = results[0].text; // Lưu kết quả
                resultTextarea.value = extractedText;
                incrementUsageCount(); // Tăng bộ đếm sau khi thành công
            } else {
                throw new Error("Không nhận được kết quả OCR.");
            }

        } catch (error) {
            resultTextarea.value = `Đã xảy ra lỗi: ${error.message}`;
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

            saveAs(blob, filename); // Sử dụng FileSaver.js

        } catch (error) {
            alert(error.message);
        }
    };

    copyButton.addEventListener('click', () => navigator.clipboard.writeText(resultTextarea.value).then(() => alert('Đã sao chép!')));
    exportWordButton.addEventListener('click', () => handleExport('docx'));
    exportExcelButton.addEventListener('click', () => handleExport('xlsx'));
});