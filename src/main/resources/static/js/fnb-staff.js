document.addEventListener('DOMContentLoaded', () => {
    // Live clock
    const clockEl = document.getElementById('live-clock');
    if (clockEl) {
        setInterval(() => {
            const now = new Date();
            clockEl.textContent = now.toLocaleTimeString('vi-VN');
        }, 1000);
    }

    // Search filter for Confirmed Reservations (Check-in)
    const confirmedSearchInput = document.getElementById('confirmedSearchInput');
    const confirmedCards = document.querySelectorAll('.confirmed-card');
    if (confirmedSearchInput) {
        confirmedSearchInput.addEventListener('input', (e) => {
            const query = e.target.value.toLowerCase().trim();
            confirmedCards.forEach(card => {
                const textContent = card.textContent.toLowerCase();
                if (textContent.includes(query)) {
                    card.style.display = '';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    }
});

function openApproveModal(reservationId, partySize) {
    const modal = document.getElementById('approveModal');
    const form = document.getElementById('approveForm');
    form.action = `/fnb/dining/reservations/${reservationId}/approve`;
    
    // Optionally: filter select options by partySize
    const select = form.querySelector('select[name="diningTableId"]');
    Array.from(select.options).forEach(opt => {
        if (opt.value === "") return;
        const capacity = parseInt(opt.textContent.match(/\((\d+)\sngười\)/)[1]);
        if (capacity < partySize) {
            opt.style.display = 'none'; // Hide tables too small
        } else {
            opt.style.display = '';
        }
    });

    // Reset the select and trigger change to update custom UI
    select.selectedIndex = 0;
    select.dispatchEvent(new Event('change'));

    modal.classList.add('show');
}

// Biến lưu trữ reservationId hiện tại đang check-in
let currentCheckinReservationId = null;

function openCheckinModal(reservationId) {
    currentCheckinReservationId = reservationId;
    const modal = document.getElementById('checkinModal');
    const form = document.getElementById('checkinForm');
    const bypassForm = document.getElementById('bypassForm');
    form.action = `/fnb/dining/reservations/${reservationId}/checkin`;
    bypassForm.action = `/fnb/dining/reservations/${reservationId}/checkin`;

    // Reset panel tra cứu số phòng
    document.getElementById('roomVerifySection').style.display = 'none';
    document.getElementById('roomVerify-result').style.display = 'none';
    document.getElementById('roomVerify-error').style.display = 'none';
    document.getElementById('verifyRoomNumber').value = '';
    document.getElementById('checkinOtpInput').value = '';

    modal.classList.add('show');
}

function closeModals() {
    document.querySelectorAll('.modal-backdrop').forEach(m => m.classList.remove('show'));
}

function toggleRoomVerify() {
    const section = document.getElementById('roomVerifySection');
    const isVisible = section.style.display !== 'none';
    section.style.display = isVisible ? 'none' : 'block';
    if (!isVisible) {
        // Focus vào ô nhập số phòng khi mở
        setTimeout(() => document.getElementById('verifyRoomNumber').focus(), 100);
    }
}

async function verifyByRoom() {
    const roomNumber = document.getElementById('verifyRoomNumber').value.trim();
    const errorEl = document.getElementById('roomVerify-error');
    const resultEl = document.getElementById('roomVerify-result');
    const btn = document.getElementById('btnVerifyRoom');

    if (!roomNumber) {
        errorEl.textContent = 'Vui lòng nhập số phòng.';
        errorEl.style.display = 'block';
        resultEl.style.display = 'none';
        return;
    }

    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';
    btn.disabled = true;
    errorEl.style.display = 'none';
    resultEl.style.display = 'none';

    try {
        const resp = await fetch(
            `/fnb/dining/reservations/${currentCheckinReservationId}/verify-by-room?roomNumber=${encodeURIComponent(roomNumber)}`
        );

        if (!resp.ok) {
            const text = await resp.text();
            throw new Error(text || 'Số phòng không khớp với khách hàng.');
        }

        const data = await resp.json();

        // Hiện thông tin đặt bàn
        document.getElementById('rv-customer').textContent = data.customerName;
        document.getElementById('rv-room').textContent = 'Phòng ' + data.roomNumber;
        document.getElementById('rv-table').textContent = data.tableNumber;
        document.getElementById('rv-time').textContent = data.startTime + ' – ' + data.endTime;

        const specialRow = document.getElementById('rv-special-row');
        const specialEl = document.getElementById('rv-special');
        if (data.specialRequests && data.specialRequests.trim() !== '') {
            specialEl.textContent = data.specialRequests;
            specialRow.style.display = '';
        } else {
            specialRow.style.display = 'none';
        }

        resultEl.style.display = 'block';

    } catch (err) {
        errorEl.style.display = 'block';
        document.getElementById('roomVerify-error-text').textContent = err.message;
    } finally {
        btn.innerHTML = '<i class="fa-solid fa-search"></i> Kiểm tra';
        btn.disabled = false;
    }

}

// Walk-in modal: Bước 1 - Mở modal
function openWalkinModal() {
    resetWalkinModal();
    document.getElementById('walkinModal').classList.add('show');
}

function closeWalkinModal() {
    document.getElementById('walkinModal').classList.remove('show');
}

function resetWalkinModal() {
    document.getElementById('walkin-step1').style.display = 'block';
    document.getElementById('walkin-step2').style.display = 'none';
    document.getElementById('walkinRoomNumber').value = '';
    document.getElementById('walkin-error').style.display = 'none';
    document.getElementById('walkin-error').textContent = '';

    // Reset table select in Walkin modal
    const tableSelect = document.querySelector('#walkinForm select[name="tableId"]');
    if (tableSelect) {
        tableSelect.selectedIndex = 0;
        tableSelect.dispatchEvent(new Event('change'));
    }
}

// Walk-in modal: Bước 2 - Tra cứu số phòng bằng AJAX
async function searchWalkinRoom() {
    const roomNumber = document.getElementById('walkinRoomNumber').value.trim();
    const errorEl = document.getElementById('walkin-error');
    const btn = document.getElementById('btnSearchRoom');

    if (!roomNumber) {
        errorEl.textContent = 'Vui lòng nhập số phòng.';
        errorEl.style.display = 'block';
        return;
    }

    // Hiển thị trạng thái đang tải
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang tra cứu...';
    btn.disabled = true;
    errorEl.style.display = 'none';

    try {
        const response = await fetch(`/fnb/dining/verify-room?roomNumber=${encodeURIComponent(roomNumber)}`);
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Không tìm thấy phòng.');
        }
        const data = await response.json();

        // Hiện thông tin khách lên Bước 2
        document.getElementById('walkin-customer-name').textContent = data.customerName;
        document.getElementById('walkin-room-display').innerHTML = `<i class="fa-solid fa-door-open"></i> Phòng ${data.roomNumber}`;
        document.getElementById('walkinRoomBookingId').value = data.roomBookingId;

        // Chuyển sang Bước 2
        document.getElementById('walkin-step1').style.display = 'none';
        document.getElementById('walkin-step2').style.display = 'block';

    } catch (err) {
        errorEl.textContent = err.message || 'Không tìm thấy phòng đang có khách lưu trú. Kiểm tra lại số phòng.';
        errorEl.style.display = 'block';
    } finally {
        btn.innerHTML = '<i class="fa-solid fa-search"></i> Tra cứu';
        btn.disabled = false;
    }
}

function checkoutTable(tableId) {
    const modal = document.getElementById('checkoutModal');
    const btnCheckoutBill = document.getElementById('btnCheckoutBill');
    const btnGoToPos = document.getElementById('btnGoToPos');
    const formCancelEmpty = document.getElementById('cancelEmptyForm');
    
    if(btnCheckoutBill) {
        btnCheckoutBill.href = '/fnb/dining/tables/' + tableId + '/bill';
    }
    if(btnGoToPos) {
        btnGoToPos.href = '/fnb/pos/' + tableId;
    }
    if(formCancelEmpty) {
        formCancelEmpty.action = '/fnb/dining/tables/' + tableId + '/cancel-empty';
    }
    
    modal.classList.add('show');
}

function closeCheckoutModal() {
    document.getElementById('checkoutModal').classList.remove('show');
}

function openCancelModal(reservationId) {
    const modal = document.getElementById('cancelModal');
    const form = document.getElementById('cancelForm');
    form.action = `/fnb/dining/reservations/${reservationId}/staff-cancel`;
    modal.classList.add('show');
}

function openCancelEmptyConfirmModal() {
    document.getElementById('cancelEmptyConfirmModal').classList.add('show');
}

function closeCancelEmptyConfirmModal() {
    document.getElementById('cancelEmptyConfirmModal').classList.remove('show');
}

// --- Custom Select Initializer ---
function initCustomSelects() {
    const selects = document.querySelectorAll('select.heritage-select');
    selects.forEach(select => {
        // Only init if not already initialized
        if (select.nextElementSibling && select.nextElementSibling.classList.contains('fnb-custom-select-container')) {
            return;
        }

        const wrapper = document.createElement('div');
        wrapper.className = 'fnb-custom-select-container';
        
        // Insert wrapper after select, then move select into wrapper
        select.parentNode.insertBefore(wrapper, select.nextSibling);
        wrapper.appendChild(select);

        const trigger = document.createElement('div');
        trigger.className = 'fnb-select-trigger';
        
        const span = document.createElement('span');
        span.textContent = select.options[select.selectedIndex]?.text || 'Chọn...';
        trigger.appendChild(span);

        const icon = document.createElement('i');
        icon.className = 'fa-solid fa-chevron-down';
        trigger.appendChild(icon);

        const optionsDiv = document.createElement('div');
        optionsDiv.className = 'fnb-select-options';

        // Function to rebuild options
        const buildOptions = () => {
            optionsDiv.innerHTML = '';
            Array.from(select.options).forEach((option, index) => {
                if (option.style.display === 'none') return; // Skip hidden options
                
                const optDiv = document.createElement('div');
                optDiv.className = 'fnb-option';
                if (option.selected) optDiv.classList.add('selected');
                
                // Add icon if it's a valid table option (value not empty)
                if (option.value !== '') {
                    optDiv.innerHTML = `<i class="fa-solid fa-chair" style="color:var(--color-gold); font-size:12px;"></i> ${option.text}`;
                } else {
                    optDiv.textContent = option.text;
                }

                optDiv.addEventListener('click', (e) => {
                    e.stopPropagation();
                    select.selectedIndex = index;
                    span.textContent = option.text;
                    wrapper.classList.remove('open');
                    
                    // Trigger change event
                    select.dispatchEvent(new Event('change'));
                    
                    // Update selected class
                    Array.from(optionsDiv.children).forEach(c => c.classList.remove('selected'));
                    optDiv.classList.add('selected');
                });
                optionsDiv.appendChild(optDiv);
            });
        };

        buildOptions();

        wrapper.appendChild(trigger);
        wrapper.appendChild(optionsDiv);

        // Toggle open/close
        trigger.addEventListener('click', (e) => {
            e.stopPropagation();
            // Close other open selects
            document.querySelectorAll('.fnb-custom-select-container').forEach(c => {
                if (c !== wrapper) c.classList.remove('open');
            });
            // Rebuild options in case they were filtered dynamically
            buildOptions();
            wrapper.classList.toggle('open');
        });

        // Sync when select value changes programmatically
        select.addEventListener('change', () => {
            span.textContent = select.options[select.selectedIndex]?.text || 'Chọn...';
            buildOptions();
        });
    });

    // Close on click outside
    document.addEventListener('click', () => {
        document.querySelectorAll('.fnb-custom-select-container').forEach(c => c.classList.remove('open'));
    });
}

document.addEventListener('DOMContentLoaded', initCustomSelects);

