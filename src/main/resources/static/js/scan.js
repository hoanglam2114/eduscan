// Configuration
const MAX_FILES = 5;
const MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
const ALLOWED_TYPES = ['image/jpeg', 'image/jpg', 'image/png'];
const API_BASE_URL = window.location.origin; // Use current domain

console.log('Scan.js loaded successfully');

// State
let selectedFiles = [];
let scanResults = [];

// DOM Elements
const fileInput = document.getElementById('fileInput');
const uploadArea = document.getElementById('uploadArea');
const previewSection = document.getElementById('previewSection');
const previewContainer = document.getElementById('previewContainer');
const imageCount = document.getElementById('imageCount');
const scanBtn = document.getElementById('scanBtn');
const loadingSection = document.getElementById('loadingSection');
const resultsSection = document.getElementById('resultsSection');
const resultsContainer = document.getElementById('resultsContainer');
const saveHistoryBtn = document.getElementById('saveHistoryBtn');
const scanAgainBtn = document.getElementById('scanAgainBtn');

// Event Listeners
fileInput.addEventListener('change', handleFileSelect);
uploadArea.addEventListener('click', () => fileInput.click());
uploadArea.addEventListener('dragover', handleDragOver);
uploadArea.addEventListener('dragleave', handleDragLeave);
uploadArea.addEventListener('drop', handleDrop);
scanBtn.addEventListener('click', performOCR);
saveHistoryBtn.addEventListener('click', saveToHistory);
scanAgainBtn.addEventListener('click', resetScan);

// File Selection Handler
function handleFileSelect(e) {
    console.log('handleFileSelect triggered');
    console.log('Files from input:', e.target.files);
    const files = Array.from(e.target.files);
    console.log('Files array:', files);
    addFiles(files);
}

// Drag and Drop Handlers
function handleDragOver(e) {
    e.preventDefault();
    uploadArea.classList.add('drag-over');
}

function handleDragLeave(e) {
    e.preventDefault();
    uploadArea.classList.remove('drag-over');
}

function handleDrop(e) {
    e.preventDefault();
    uploadArea.classList.remove('drag-over');
    const files = Array.from(e.dataTransfer.files);
    addFiles(files);
}

// Add Files to Selection
function addFiles(files) {
    console.log('addFiles called with:', files.length, 'files');

    // Filter valid files
    const validFiles = files.filter(file => {
        console.log('Checking file:', file.name, file.type, file.size);

        // Check file type
        if (!ALLOWED_TYPES.includes(file.type)) {
            console.warn('Invalid file type:', file.type);
            showToast(`${file.name} is not a valid image type (JPG, PNG only)`, 'warning');
            return false;
        }
        // Check file size
        if (file.size > MAX_FILE_SIZE) {
            console.warn('File too large:', file.size);
            showToast(`${file.name} exceeds 2MB limit`, 'warning');
            return false;
        }
        console.log('File is valid:', file.name);
        return true;
    });

    console.log('Valid files:', validFiles.length);

    // Check max files limit
    const remainingSlots = MAX_FILES - selectedFiles.length;
    if (validFiles.length > remainingSlots) {
        showToast(`You can only upload ${MAX_FILES} images total`, 'warning');
        validFiles.splice(remainingSlots);
    }

    // Add to selected files
    selectedFiles = [...selectedFiles, ...validFiles];
    console.log('Total selected files now:', selectedFiles.length);
    updatePreview();
}

// Update Preview Display
function updatePreview() {
    console.log('updatePreview called, files:', selectedFiles.length);

    if (selectedFiles.length === 0) {
        console.log('No files, hiding preview');
        previewSection.style.display = 'none';
        return;
    }

    console.log('Showing preview section');
    previewSection.style.display = 'block';
    imageCount.textContent = selectedFiles.length;
    previewContainer.innerHTML = '';

    selectedFiles.forEach((file, index) => {
        console.log(`Creating preview for file ${index}:`, file.name);

        const col = document.createElement('div');
        col.className = 'col-6 col-md-4 col-lg-3';

        const reader = new FileReader();
        reader.onload = (e) => {
            console.log(`FileReader loaded for ${file.name}`);
            col.innerHTML = `
                <div class="preview-card">
                    <img src="${e.target.result}" alt="${file.name}">
                    <button class="remove-btn" onclick="removeFile(${index})">
                        <i class="fas fa-times"></i>
                    </button>
                    <div class="file-name">${file.name}</div>
                </div>
            `;
        };
        reader.onerror = (error) => {
            console.error('FileReader error:', error);
        };
        reader.readAsDataURL(file);

        previewContainer.appendChild(col);
    });

    console.log('Preview updated successfully');
}

// Remove File from Selection
function removeFile(index) {
    selectedFiles.splice(index, 1);
    updatePreview();
}

// Perform OCR
async function performOCR() {
    console.log('performOCR called');
    console.log('Selected files:', selectedFiles.length);

    if (selectedFiles.length === 0) {
        showToast('Please select at least one image', 'warning');
        return;
    }

    // Show loading
    previewSection.style.display = 'none';
    loadingSection.style.display = 'block';
    resultsSection.style.display = 'none';

    // Prepare FormData
    const formData = new FormData();
    selectedFiles.forEach((file, index) => {
        console.log(`Adding file ${index + 1}:`, file.name, file.type, file.size);
        formData.append('files', file);
    });

    try {
        console.log('Calling API:', `${API_BASE_URL}/api/ocr/scan-multiple`);

        // Call API
        const response = await fetch(`${API_BASE_URL}/api/ocr/scan-multiple`, {
            method: 'POST',
            body: formData
        });

        console.log('Response status:', response.status);
        console.log('Response ok:', response.ok);

        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error response:', errorText);
            throw new Error(`Server error: ${response.status}`);
        }

        const results = await response.json();
        console.log('Results received:', results);
        scanResults = results;

        // Display results
        displayResults(results);

    } catch (error) {
        console.error('Error during OCR:', error);
        showToast('Failed to process images: ' + error.message, 'danger');
        loadingSection.style.display = 'none';
        previewSection.style.display = 'block';
    }
}

// Display Results
function displayResults(results) {
    console.log('displayResults called with:', results);

    loadingSection.style.display = 'none';
    resultsSection.style.display = 'block';
    resultsContainer.innerHTML = '';

    if (!results || results.length === 0) {
        console.error('No results to display');
        resultsContainer.innerHTML = '<div class="alert alert-warning">No results returned from server</div>';
        return;
    }

    results.forEach((result, index) => {
        console.log(`Processing result ${index}:`, result);

        const card = document.createElement('div');
        card.className = `result-card ${result.success ? '' : 'error'} fade-in`;

        if (result.success) {
            card.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-3">
                    <h6 class="mb-0"><i class="fas fa-file-image me-2"></i>${result.fileName || 'Unknown'}</h6>
                    <div>
                        <button class="btn btn-sm btn-outline-primary me-2" onclick="copyText(${index})">
                            <i class="fas fa-copy me-1"></i>Copy
                        </button>
                        <button class="btn btn-sm btn-outline-secondary" onclick="exportText(${index})">
                            <i class="fas fa-download me-1"></i>Export
                        </button>
                    </div>
                </div>
                <div class="result-text" id="result-${index}">${escapeHtml(result.extractedText || 'No text extracted')}</div>
            `;
        } else {
            card.innerHTML = `
                <div class="d-flex align-items-center">
                    <i class="fas fa-exclamation-circle text-danger me-2"></i>
                    <div>
                        <h6 class="mb-1">${result.fileName || 'Unknown file'}</h6>
                        <p class="text-danger mb-0">${result.error || 'Failed to extract text'}</p>
                    </div>
                </div>
            `;
        }

        resultsContainer.appendChild(card);
    });

    console.log('Results displayed successfully');
}

// Copy Text to Clipboard
function copyText(index) {
    const text = scanResults[index].extractedText;
    navigator.clipboard.writeText(text).then(() => {
        showToast('Text copied to clipboard!', 'success');
    }).catch(err => {
        showToast('Failed to copy text', 'danger');
    });
}

// Export Text as TXT File
function exportText(index) {
    const result = scanResults[index];
    const blob = new Blob([result.extractedText], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${result.fileName.replace(/\.[^/.]+$/, '')}_extracted.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    showToast('Text exported successfully!', 'success');
}

// Save to History
function saveToHistory() {
    const history = JSON.parse(localStorage.getItem('ocrHistory') || '[]');

    const historyItem = {
        id: Date.now(),
        timestamp: new Date().toISOString(),
        results: scanResults.filter(r => r.success).map(r => ({
            fileName: r.fileName,
            extractedText: r.extractedText
        }))
    };

    history.unshift(historyItem);
    localStorage.setItem('ocrHistory', JSON.stringify(history));

    showToast('Saved to history!', 'success');

    // Redirect to history after a short delay
    setTimeout(() => {
        window.location.href = 'history.html';
    }, 1000);
}

// Reset Scan
function resetScan() {
    selectedFiles = [];
    scanResults = [];
    fileInput.value = '';
    previewSection.style.display = 'none';
    loadingSection.style.display = 'none';
    resultsSection.style.display = 'none';
    previewContainer.innerHTML = '';
}

// Show Toast Notification
function showToast(message, type = 'info') {
    // Create toast container if it doesn't exist
    let toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999;';
        document.body.appendChild(toastContainer);
    }

    // Create toast
    const toast = document.createElement('div');
    toast.className = `alert alert-${type} alert-dismissible fade show`;
    toast.style.cssText = 'min-width: 250px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);';
    toast.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    toastContainer.appendChild(toast);

    // Auto remove after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 150);
    }, 3000);
}

// Escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Make functions global for inline onclick
window.removeFile = removeFile;
window.copyText = copyText;
window.exportText = exportText;

// Verify all elements exist on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('=== EduScan Scan Page Loaded ===');
    console.log('fileInput:', fileInput);
    console.log('uploadArea:', uploadArea);
    console.log('previewSection:', previewSection);
    console.log('previewContainer:', previewContainer);
    console.log('scanBtn:', scanBtn);
    console.log('loadingSection:', loadingSection);
    console.log('resultsSection:', resultsSection);

    if (!fileInput) console.error('ERROR: fileInput not found!');
    if (!uploadArea) console.error('ERROR: uploadArea not found!');
    if (!previewSection) console.error('ERROR: previewSection not found!');
    if (!scanBtn) console.error('ERROR: scanBtn not found!');

    console.log('All elements loaded successfully');
    console.log('=== Ready to scan ===');
});