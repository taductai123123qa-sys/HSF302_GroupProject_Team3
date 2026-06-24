function switchTab(tabId) {
    // Remove active class from all buttons and contents
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    // Add active class to clicked button and target content
    event.currentTarget.classList.add('active');
    document.getElementById('tab-' + tabId).classList.add('active');
}

function toggleForm(type) {
    const grid = document.getElementById('grid-' + type);
    grid.classList.toggle('form-hidden');
}

// removed edit functions

function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = 'fa-check-circle';
    if (type === 'error') icon = 'fa-circle-xmark';
    
    toast.innerHTML = `
        <i class="fa-solid ${icon}"></i>
        <div class="toast-msg">${message}</div>
    `;
    
    container.appendChild(toast);
    
    // Trigger animation
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Remove after 3 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 3000);
}

// --- Custom Confirm Modal for Form Submission ---
let formToSubmit = null;

function confirmDeleteForm(btnElement, message) {
    formToSubmit = btnElement.closest('form');
    document.getElementById('customConfirmMsg').innerText = message;
    document.getElementById('customConfirmModal').classList.add('active');
}

function closeConfirmModal() {
    document.getElementById('customConfirmModal').classList.remove('active');
    formToSubmit = null;
}

const btnConfirmAction = document.getElementById('btnConfirmAction');
if (btnConfirmAction) {
    btnConfirmAction.addEventListener('click', function() {
        if (formToSubmit) {
            formToSubmit.submit();
        }
        closeConfirmModal();
    });
}
