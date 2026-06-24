/**
 * Minimalist JS for Receptionist Dashboard
 * Focuses on UI toggles (Modals, Room Filters) without heavy dependencies.
 */

document.addEventListener("DOMContentLoaded", () => {

    /* =======================================
       1. ROOM FILTERING (Sơ đồ phòng)
       ======================================= */
    const filterBtns = document.querySelectorAll('.filter-btn');
    const roomCards = document.querySelectorAll('.room-card');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            // Update active state on buttons
            filterBtns.forEach(b => b.classList.remove('active'));
            e.target.classList.add('active');

            const filterType = e.target.getAttribute('data-filter');

            // Toggle visibility using minimalist CSS-driven approach
            roomCards.forEach(card => {
                if (filterType === 'all') {
                    card.style.display = 'block';
                } else {
                    if (card.classList.contains(filterType)) {
                        card.style.display = 'block';
                    } else {
                        card.style.display = 'none';
                    }
                }
            });
        });
    });

    /* =======================================
       2. MODAL TOGGLE (Check-in/Check-out)
       ======================================= */
    const checkoutBtns = document.querySelectorAll('.checkout-btn');
    const modal = document.getElementById('checkinModal');
    const btnCloseModal = document.getElementById('closeModal');

    // Đổ danh sách phòng trống từ Server vào Cache JS
    let availableRoomsCache = [];
    let accompanyingGuestCount = 0;

    fetch('/receptionist/api/available-rooms')
        .then(res => res.json())
        .then(data => availableRoomsCache = data)
        .catch(err => console.error("Error fetching available rooms:", err));

    document.querySelectorAll('.checkin-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const bookingItem = e.target.closest('.booking-item');
            if (!bookingItem) return;

            const dataDiv = bookingItem.querySelector('.booking-data');
            if (!dataDiv) return;

            document.getElementById('modalBookingId').value = dataDiv.getAttribute('data-booking-id');
            document.getElementById('modalGuestName').value = dataDiv.getAttribute('data-guest-name');
            document.getElementById('modalGuestIdentity').value = dataDiv.getAttribute('data-guest-identity') || '';

            document.getElementById('accompanyingGuestsList').innerHTML = '';
            window.accompanyingGuestCount = 0;

            const roomBoxes = document.getElementById('roomSelectionBoxes');
            roomBoxes.innerHTML = '';

            const cartItems = dataDiv.querySelectorAll('.cart-item-data');
            cartItems.forEach(item => {
                const catId = item.getAttribute('data-category-id');
                const catName = item.getAttribute('data-category-name');
                const quantity = parseInt(item.getAttribute('data-quantity'));

                // Lọc ra các phòng vật lý trống thuộc đúng phân loại này
                const roomsInCat = availableRoomsCache.filter(r => r.categoryId == catId);

                const groupDiv = document.createElement('div');
                groupDiv.style.marginBottom = '15px';
                groupDiv.innerHTML = `<label style="font-weight:600; color:var(--color-primary);">Hạng phòng: ${catName} (Cần gán: ${quantity} phòng)</label>`;

                // Duyệt vòng lặp sinh đủ số lượng ô gán phòng theo đúng yêu cầu giỏ hàng
                for (let i = 0; i < quantity; i++) {
                    const select = document.createElement('select');
                    select.className = 'form-control room-select';
                    select.style.marginBottom = '8px';

                    let options = `<option value="">-- Chọn mã phòng --</option>`;
                    roomsInCat.forEach(r => {
                        options += `<option value="${r.roomId}">${r.roomNumber}</option>`;
                    });
                    select.innerHTML = options;
                    groupDiv.appendChild(select);
                }
                roomBoxes.appendChild(groupDiv);
            });

            if (modal) {
                modal.classList.add('active');
            }
        });
    });

    // Close Modal
    if (btnCloseModal) {
        btnCloseModal.addEventListener('click', () => {
            if (modal) modal.classList.remove('active');
        });
    }

    // Close modal when clicking outside
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    }

    // Mock Check-out action (Can be expanded to fetch Folio data)
    checkoutBtns.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const roomNumber = e.target.getAttribute('data-room');
            if (confirm(`Tiến hành xuất hóa đơn tổng hợp (Guest Folio) cho phòng ${roomNumber}?`)) {
                // Redirect to invoice page or trigger API
                console.log(`Checkout processing for ${roomNumber}`);
                alert("Đang chuyển hướng sang trang Hóa đơn...");
            }
        });
    });

});

// Chú thích: Thuật toán sinh động thẻ nhập liệu kèm array index Binding (accompanyingGuests[x])
window.addAccompanyingGuest = function () {
    const list = document.getElementById('accompanyingGuestsList');
    if (typeof window.accompanyingGuestCount === 'undefined') {
        window.accompanyingGuestCount = 0;
    }
    const index = window.accompanyingGuestCount++;
    const div = document.createElement('div');
    div.style = 'border:1px dashed #ccc; padding:10px; margin-bottom:10px; border-radius:5px; background:#f9f9f9;';
    div.innerHTML = `
        <div class="form-group" style="margin-bottom:8px;">
            <label style="font-size:12px;">Tên người đi cùng</label>
            <input type="text" class="form-control" name="accompanyingGuests[${index}].fullName" required>
        </div>
        <div class="form-group" style="margin-bottom:0;">
            <label style="font-size:12px;">CCCD / Passport</label>
            <input type="text" class="form-control" name="accompanyingGuests[${index}].identityNumber">
        </div>
    `;
    list.appendChild(div);
};

// Chú thích: Thuật toán sinh thẻ ẩn và Submit form
window.submitCheckInForm = function () {
    const container = document.getElementById('selectedRoomsContainer');
    container.innerHTML = '';

    const selects = document.querySelectorAll('#roomSelectionBoxes select');
    let hasError = false;

    selects.forEach(selectBox => {
        const roomId = selectBox.value;
        if (!roomId || roomId === "") {
            alert('Vui lòng gán phòng cho tất cả các hạng mục!');
            hasError = true;
            return;
        }
        const hiddenInput = document.createElement('input');
        hiddenInput.type = 'hidden';
        hiddenInput.name = 'selectedRoomIds';
        hiddenInput.value = roomId;
        container.appendChild(hiddenInput);
    });

    if (!hasError) {
        document.getElementById('checkInForm').submit();
    }
};
