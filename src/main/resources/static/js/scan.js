// --- Cấu hình ---
const MAX_FILES = 10;
const API_BASE_URL = "/api";

// --- Các đối tượng DOM ---
const fileInput = document.getElementById('fileInput');
const uploadArea = document.getElementById('uploadArea');
const previewSection = document.getElementById('previewSection');
const previewContainer = document.getElementById('previewContainer');
const imageCount = document.getElementById('imageCount');
const scanBtn = document.getElementById('scanBtn');
const scanAgainBtn = document.getElementById('scanAgainBtn');
const loadingSection = document.getElementById('loadingSection');
const resultsSection = document.getElementById('resultsSection');
const resultsContainer = document.getElementById('resultsContainer');
const providerSelect = document.getElementById('providerSelect');
// DOM cho khu vực export
const exportSection = document.getElementById('exportSection');
const exportFormatSelect = document.getElementById('exportFormatSelect');
const exportBtn = document.getElementById('exportBtn');


// --- State ---
let selectedFiles = [];
let scanResults = []; // Biến để lưu kết quả OCR

// --- Event Listeners ---
fileInput.addEventListener('change', handleFileSelect);
uploadArea.addEventListener('click', () => fileInput.click());
scanBtn.addEventListener('click', performOCR);
scanAgainBtn.addEventListener('click', resetScan);
exportBtn.addEventListener('click', handleCombinedExport); // Event cho nút export tổng

// --- Các hàm xử lý ---

function handleFileSelect(event) {
    const files = Array.from(event.target.files);
    if (files.length > MAX_FILES) {
        alert(`Bạn chỉ có thể chọn tối đa ${MAX_FILES} file.`);
        return;
    }
    selectedFiles = files;
    updatePreview();
}

function updatePreview() {
    previewContainer.innerHTML = '';
    if (selectedFiles.length > 0) {
        selectedFiles.forEach(file => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.className = 'preview-image';
                previewContainer.appendChild(img);
            };
            reader.readAsDataURL(file);
        });
        previewSection.style.display = 'block';
        imageCount.textContent = `${selectedFiles.length} file(s) selected`;
        scanBtn.disabled = false;
    } else {
        previewSection.style.display = 'none';
        scanBtn.disabled = true;
    }
}

async function performOCR() {
    if (selectedFiles.length === 0) return;

    uploadArea.style.display = 'none';
    previewSection.style.display = 'none';
    loadingSection.style.display = 'flex';
    resultsSection.style.display = 'none';

    const formData = new FormData();
    selectedFiles.forEach(file => formData.append('files', file));
    const provider = providerSelect.value;

    try {
        const response = await fetch(`${API_BASE_URL}/upload?provider=${provider}`, {
            method: 'POST',
            body: formData,
        });
        if (!response.ok) throw new Error(await response.text());

        const results = await response.json();
        scanResults = results; // Lưu kết quả vào biến state
        displayResults(results);

    } catch (error) {
        console.error('OCR Error:', error);
        alert('OCR thất bại: ' + error.message);
        resetScan();
    } finally {
        loadingSection.style.display = 'none';
    }
}

function displayResults(results) {
    resultsContainer.innerHTML = '';
    const hasSuccessfulResults = results.some(r => r.text);

    results.forEach(result => {
        const resultCard = document.createElement('div');
        resultCard.className = 'result-card';
        let contentHTML = `<h4>File: ${result.fileName}</h4>`;
        if (result.text) {
            contentHTML += `<textarea readonly rows="5">${result.text}</textarea>`;
        } else {
            contentHTML += `<p class="error-text">Lỗi: ${result.error}</p>`;
        }
        resultCard.innerHTML = contentHTML;
        resultsContainer.appendChild(resultCard);
    });

    resultsSection.style.display = 'block';
    // Chỉ hiển thị khu vực export nếu có ít nhất 1 kết quả thành công
    if (hasSuccessfulResults) {
        exportSection.style.display = 'block';
    }
}

// Hàm xử lý download tổng hợp
async function handleCombinedExport() {
    const format = exportFormatSelect.value;

    // Tạo nội dung text tổng hợp
    const combinedText = scanResults
        .filter(result => result.text)
        .map(result => `--- Kết quả từ file: ${result.fileName} ---\n${result.text}\n`)
        .join("\n" + "=".repeat(50) + "\n");

    if (!combinedText.trim()) {
        alert("Không có văn bản để tải xuống.");
        return;
    }

    // Vô hiệu hóa nút để tránh click nhiều lần
    exportBtn.disabled = true;
    exportBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Đang tạo...';

    try {
        // Gọi API để tạo file
        const { blob, fileName } = await exportFile(combinedText, format);

        // Logic tải file
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `tong_hop_${fileName}`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Export Error:', error);
        alert('Tạo file thất bại: ' + error.message);
    } finally {
        // Bật lại nút sau khi hoàn thành
        exportBtn.disabled = false;
        exportBtn.innerHTML = '<i class="fas fa-download me-2"></i>Tải xuống';
    }
}

// Hàm gọi API để export (không đổi)
async function exportFile(text, format) {
    const response = await fetch(`${API_BASE_URL}/export?format=${format}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: text })
    });
    if (!response.ok) throw new Error(await response.text());

    const blob = await response.blob();
    const contentDisposition = response.headers.get('content-disposition');
    let fileName = `result.${format}`;
    if (contentDisposition) {
        const match = contentDisposition.match(/filename="(.+)"/);
        if (match) fileName = match[1];
    }
    return { blob, fileName };
}

function resetScan() {
    selectedFiles = [];
    scanResults = [];
    fileInput.value = '';
    uploadArea.style.display = 'block';
    previewSection.style.display = 'none';
    loadingSection.style.display = 'none';
    resultsSection.style.display = 'none';
    exportSection.style.display = 'none';
    scanBtn.disabled = true;
}