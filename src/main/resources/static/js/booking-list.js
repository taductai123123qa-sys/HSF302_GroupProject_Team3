// Modal Logic cho Danh sách Booking
const guestModal = document.getElementById('guestListModal');
const closeModalBtn = document.getElementById('closeGuestModal');
const guestModalBody = document.getElementById('guestModalBody');

// Đóng modal
if (closeModalBtn) {
    closeModalBtn.addEventListener('click', () => {
        guestModal.classList.remove('active');
    });
}
window.addEventListener('click', (e) => {
    if (e.target === guestModal) {
        guestModal.classList.remove('active');
    }
});

// Hàm gọi Ajax và mở Modal
window.openGuestModal = function (bookingId) {
    // Hiển thị modal với hiệu ứng loading
    guestModal.classList.add('active');
    guestModalBody.innerHTML = `
        <div style="text-align: center; padding: 20px;">
            <i class="fa-solid fa-spinner fa-spin" style="font-size: 2em; color: var(--color-primary);"></i>
            <p style="margin-top: 10px;">Đang tải dữ liệu...</p>
        </div>
    `;

    // Gọi API lấy dữ liệu
    fetch(`/api/v1/bookings/${bookingId}/guests`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                let html = '';

                // Render Khách đại diện
                html += `
                    <h4 style="margin-bottom: 10px; font-size: 1em; color: var(--color-text);">Khách đại diện (Primary Guest)</h4>
                    <div class="guest-item primary">
                        <i class="fa-solid fa-user-check"></i>
                        <div>
                            <strong>${data.primaryGuest.name}</strong><br>
                            <span style="font-size: 0.85em; color: var(--color-text-light);">CCCD/Passport: ${data.primaryGuest.identity}</span>
                        </div>
                    </div>
                `;

                // Render Khách đi cùng
                if (data.accompanyingGuests && data.accompanyingGuests.length > 0) {
                    html += `<h4 style="margin-top: 20px; margin-bottom: 10px; font-size: 1em; color: var(--color-text);">Người đi cùng (Accompanying Guests)</h4>`;
                    data.accompanyingGuests.forEach(guest => {
                        html += `
                            <div class="guest-item">
                                <i class="fa-solid fa-user"></i>
                                <div>
                                    <strong>${guest.name}</strong><br>
                                    <span style="font-size: 0.85em; color: var(--color-text-light);">CCCD/Passport: ${guest.identity}</span>
                                </div>
                            </div>
                        `;
                    });
                } else {
                    html += `
                        <div style="margin-top: 20px; text-align: center; color: var(--color-text-light); padding: 15px; border: 1px dashed #ccc; border-radius: 8px;">
                            Không có dữ liệu người đi cùng.
                        </div>
                    `;
                }

                guestModalBody.innerHTML = html;
            } else {
                guestModalBody.innerHTML = `
                    <div class="alert alert-danger" style="margin: 0;">
                        Lỗi: ${data.message}
                    </div>
                `;
            }
        })
        .catch(error => {
            console.error("Error fetching guests:", error);
            guestModalBody.innerHTML = `
                <div class="alert alert-danger" style="margin: 0;">
                    Lỗi kết nối máy chủ. Vui lòng thử lại sau.
                </div>
            `;
        });
};
