// DOM Elements
const historyContainer = document.getElementById('historyContainer');
const emptyState = document.getElementById('emptyState');
const searchInput = document.getElementById('searchInput');
const sortSelect = document.getElementById('sortSelect');
const clearAllBtn = document.getElementById('clearAllBtn');

// State
let allHistory = [];
let filteredHistory = [];

// Event Listeners
document.addEventListener('DOMContentLoaded', loadHistory);
searchInput.addEventListener('input', filterHistory);
sortSelect.addEventListener('change', sortHistory);
clearAllBtn.addEventListener('click', clearAllHistory);

// Load History from localStorage
function loadHistory() {
    const historyData = localStorage.getItem('ocrHistory');
    allHistory = historyData ? JSON.parse(historyData) : [];
    filteredHistory = [...allHistory];
    displayHistory();
}

// Display History Items
function displayHistory() {
    if (filteredHistory.length === 0) {
        historyContainer.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    historyContainer.style.display = 'block';
    emptyState.style.display = 'none';
    historyContainer.innerHTML = '';

    filteredHistory.forEach((item, index) => {
        const card = createHistoryCard(item, index);
        historyContainer.appendChild(card);
    });
}

// Create History Card
function createHistoryCard(item, index) {
    const card = document.createElement('div');
    card.className = 'history-card fade-in';

    const date = new Date(item.timestamp);
    const formattedDate = formatDate(date);
    const formattedTime = formatTime(date);

    // Create results HTML
    const resultsHtml = item.results.map((result, resultIndex) => `
        <div class="mb-3">
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h6 class="mb-0">
                    <i class="fas fa-file-image text-primary me-2"></i>${result.fileName}
                </h6>
                <div>
                    <button class="btn btn-sm btn-outline-primary me-2" onclick="copyHistoryText(${index}, ${resultIndex})">
                        <i class="fas fa-copy"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-secondary" onclick="exportHistoryText(${index}, ${resultIndex})">
                        <i class="fas fa-download"></i>
                    </button>
                </div>
            </div>
            <div class="result-text">${escapeHtml(result.extractedText)}</div>
        </div>
    `).join('');

    card.innerHTML = `
        <div class="history-header">
            <div>
                <h5 class="mb-1">Scan Session</h5>
                <div class="history-date">
                    <i class="fas fa-calendar me-1"></i>${formattedDate}
                    <i class="fas fa-clock ms-3 me-1"></i>${formattedTime}
                    <span class="badge bg-secondary ms-2">${item.results.length} image${item.results.length > 1 ? 's' : ''}</span>
                </div>
            </div>
            <button class="btn btn-sm btn-outline-danger" onclick="deleteHistoryItem(${index})">
                <i class="fas fa-trash"></i>
            </button>
        </div>
        <hr>
        ${resultsHtml}
    `;

    return card;
}

// Filter History
function filterHistory() {
    const searchTerm = searchInput.value.toLowerCase().trim();

    if (searchTerm === '') {
        filteredHistory = [...allHistory];
    } else {
        filteredHistory = allHistory.filter(item => {
            return item.results.some(result =>
                result.extractedText.toLowerCase().includes(searchTerm) ||
                result.fileName.toLowerCase().includes(searchTerm)
            );
        });
    }

    sortHistory();
}

// Sort History
function sortHistory() {
    const sortOrder = sortSelect.value;

    filteredHistory.sort((a, b) => {
        const dateA = new Date(a.timestamp);
        const dateB = new Date(b.timestamp);

        if (sortOrder === 'newest') {
            return dateB - dateA;
        } else {
            return dateA - dateB;
        }
    });

    displayHistory();
}

// Copy History Text
function copyHistoryText(historyIndex, resultIndex) {
    const text = filteredHistory[historyIndex].results[resultIndex].extractedText;
    navigator.clipboard.writeText(text).then(() => {
        showToast('Text copied to clipboard!', 'success');
    }).catch(err => {
        showToast('Failed to copy text', 'danger');
    });
}

// Export History Text
function exportHistoryText(historyIndex, resultIndex) {
    const result = filteredHistory[historyIndex].results[resultIndex];
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

// Delete History Item
function deleteHistoryItem(index) {
    if (confirm('Are you sure you want to delete this scan session?')) {
        // Find the original index in allHistory
        const itemToDelete = filteredHistory[index];
        const originalIndex = allHistory.findIndex(item => item.id === itemToDelete.id);

        if (originalIndex > -1) {
            allHistory.splice(originalIndex, 1);
            localStorage.setItem('ocrHistory', JSON.stringify(allHistory));

            // Update filtered history
            filteredHistory.splice(index, 1);
            displayHistory();
            showToast('Scan session deleted', 'success');
        }
    }
}

// Clear All History
function clearAllHistory() {
    if (confirm('Are you sure you want to clear all history? This action cannot be undone.')) {
        localStorage.removeItem('ocrHistory');
        allHistory = [];
        filteredHistory = [];
        displayHistory();
        showToast('All history cleared', 'success');
    }
}

// Format Date
function formatDate(date) {
    const options = { year: 'numeric', month: 'short', day: 'numeric' };
    return date.toLocaleDateString('en-US', options);
}

// Format Time
function formatTime(date) {
    const options = { hour: '2-digit', minute: '2-digit' };
    return date.toLocaleTimeString('en-US', options);
}

// Show Toast Notification
function showToast(message, type = 'info') {
    let toastContainer = document.getElementById('toastContainer');
    if (!toastContainer) {
        toastContainer = document.createElement('div');
        toastContainer.id = 'toastContainer';
        toastContainer.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999;';
        document.body.appendChild(toastContainer);
    }

    const toast = document.createElement('div');
    toast.className = `alert alert-${type} alert-dismissible fade show`;
    toast.style.cssText = 'min-width: 250px; box-shadow: 0 4px 12px rgba(0,0,0,0.15);';
    toast.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    toastContainer.appendChild(toast);

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
window.copyHistoryText = copyHistoryText;
window.exportHistoryText = exportHistoryText;
window.deleteHistoryItem = deleteHistoryItem;