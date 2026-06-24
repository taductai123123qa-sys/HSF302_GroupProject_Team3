// Biến lưu trữ ID lịch trình đang được chọn
let selectedScheduleId = null;
let currentAvailable = null; // Số chỗ trống của lịch trình đang chọn

// Hàm cập nhật giao diện hiển thị Lịch trình chi tiết (Timeline)
function updateTimelineUI(startTimeStr, container) {
    if (!container) return;

    if (typeof rawTimelines === 'undefined' || !rawTimelines || rawTimelines.length === 0) {
        container.innerHTML = '<p class="text-muted text-center mt-3">Tour này chưa có lịch trình chi tiết.</p>';
        return;
    }

    if (!startTimeStr) return;

    const parts = startTimeStr.split(':');
    let currentDate = new Date();
    currentDate.setHours(parseInt(parts[0]), parseInt(parts[1]), 0, 0);

    let html = '';
    rawTimelines.forEach(t => {
        const h = String(currentDate.getHours()).padStart(2, '0');
        const m = String(currentDate.getMinutes()).padStart(2, '0');
        const timeLabel = h + ':' + m;

        html += `
            <div class="itinerary-day">
                <div class="day-marker">${timeLabel}</div>
                <div class="day-content">
                    <p style="white-space: pre-line; margin-bottom: 0;">${t.description}</p>
                    <small class="text-muted"><i class="far fa-clock"></i> Thời lượng: ${t.durationMinutes} phút</small>
                </div>
            </div>
        `;

        // Cộng dồn thời gian cho hoạt động tiếp theo
        currentDate.setMinutes(currentDate.getMinutes() + (t.durationMinutes || 0));
    });

    container.innerHTML = html;
}

// Hàm mở rộng/thu gọn (Toggle) chi tiết của một Lịch khởi hành
function toggleSchedule(element) {
    const container = element.closest('.schedule-item-container');
    const isAlreadyExpanded = container.classList.contains('expanded');

    // Thu gọn tất cả các lịch trình khác
    document.querySelectorAll('.schedule-item-container').forEach(el => {
        el.classList.remove('expanded', 'border-primary', 'rounded-4');
        el.classList.add('border', 'rounded-pill');

        const btn = el.querySelector('.sch-action button');
        if (btn) {
            btn.className = 'btn btn-light rounded-pill px-4 text-muted fw-bold action-btn';
            btn.innerText = 'Chọn';
        }

        const dateBadge = el.querySelector('.sch-date span');
        if (dateBadge) {
            dateBadge.className = 'text-primary fw-bold px-2 date-badge';
        }

        const panel = el.querySelector('.schedule-detail-panel');
        if (panel) panel.classList.add('d-none');
    });

    // Nếu bấm vào lịch trình đang mở, thì thu gọn lại và xóa thông tin bên thẻ (widget) Widget
    if (isAlreadyExpanded) {
        selectedScheduleId = null;
        currentAvailable = null;
        const warningEl = document.getElementById('capacity-warning');
        if (warningEl) warningEl.style.display = 'none';

        if (typeof window.updateBookUrl === 'function') window.updateBookUrl();

        const widgetDateDisplay = document.getElementById('widget-date-display');
        if (widgetDateDisplay) widgetDateDisplay.innerHTML = 'Chưa chọn <i class="fas fa-pen ms-1"></i>';

        const widgetDuration = document.getElementById('widget-duration-display');
        if (widgetDuration) widgetDuration.innerText = 'Chưa chọn';

        const widgetCapacity = document.getElementById('widget-capacity-display');
        if (widgetCapacity) widgetCapacity.innerText = 'Chưa chọn';

        return;
    }

    // Nếu bấm vào lịch trình mới, mở rộng nó ra
    selectedScheduleId = container.getAttribute('data-schedule-id');
    container.classList.add('expanded', 'border-primary', 'rounded-4');
    container.classList.remove('border', 'rounded-pill');

    const btn = container.querySelector('.sch-action button');
    if (btn) {
        btn.className = 'btn btn-primary rounded-pill px-4 action-btn';
        btn.innerText = 'Đang chọn';
    }

    const dateBadge = container.querySelector('.sch-date span');
    if (dateBadge) {
        dateBadge.className = 'badge bg-light text-primary rounded-pill px-3 py-2 fs-6 date-badge';
    }

    const panel = container.querySelector('.schedule-detail-panel');
    if (panel) panel.classList.remove('d-none');

    // Cập nhật giao diện thanh thời gian (Timeline) cho lịch trình này
    const startTimeStr = container.getAttribute('data-start-time');
    const timelineContainer = container.querySelector('.schedule-timeline-container');
    updateTimelineUI(startTimeStr, timelineContainer);

    // Cập nhật thông tin Giá, Ngày, Giờ bên Widget cột phải
    if (typeof window.updateBookUrl === 'function') window.updateBookUrl();

    const widgetDateDisplay = document.getElementById('widget-date-display');
    const dateStr = container.getAttribute('data-date');
    if (widgetDateDisplay && dateStr) {
        widgetDateDisplay.innerHTML = dateStr + ' <i class="fas fa-pen ms-1"></i>';
    }

    // Tính toán thời lượng chuyến đi (Số ngày đêm)
    const startIso = container.getAttribute('data-start-iso');
    const endIso = container.getAttribute('data-end-iso');
    const widgetDuration = document.getElementById('widget-duration-display');
    if (startIso && endIso && widgetDuration) {
        const startDate = new Date(startIso);
        const endDate = new Date(endIso);
        const diffTime = Math.abs(endDate - startDate);
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

        if (diffDays === 0) {
            widgetDuration.innerText = 'Trong ngày';
        } else {
            widgetDuration.innerText = (diffDays + 1) + ' Ngày ' + diffDays + ' Đêm';
        }
    }

    // Cập nhật Số chỗ còn trống bên Widget
    const available = container.getAttribute('data-available');
    const widgetCapacity = document.getElementById('widget-capacity-display');
    const warningEl = document.getElementById('capacity-warning');

    if (available && widgetCapacity) {
        currentAvailable = parseInt(available);
        widgetCapacity.innerText = 'Còn ' + available + ' chỗ';

        // Tự động giảm số lượng khách nếu đang chọn quá số chỗ còn lại
        const inputQuantity = document.getElementById('guest-quantity');
        if (inputQuantity) {
            let currentQty = parseInt(inputQuantity.value);
            if (currentQty > currentAvailable) {
                inputQuantity.value = currentAvailable;
                if (warningEl) {
                    document.getElementById('warning-slots').innerText = currentAvailable;
                    warningEl.style.display = 'block';
                }
            } else if (warningEl) {
                warningEl.style.display = 'none';
            }
        }
    }
}

// Biến điều khiển DOM
let btnMinus, btnPlus, inputQuantity, btnBookNow, widgetTotalPrice;

// Hàm cập nhật đường link chuyển sang trang Đặt Tour kèm theo Tham số (Pass-through)
window.updateBookUrl = function () {
    if (!inputQuantity || !btnBookNow) return;
    const quantity = parseInt(inputQuantity.value) || 1;

    // Nếu đã chọn lịch trình thì nút bấm sáng lên, truyền scheduleId và quantity vào URL
    if (selectedScheduleId) {
        btnBookNow.setAttribute('href', `/tour/tours/booking?scheduleId=${selectedScheduleId}&quantity=${quantity}`);
        btnBookNow.classList.remove('disabled');
        btnBookNow.style.opacity = "1";
        btnBookNow.style.pointerEvents = "auto";
        btnBookNow.innerText = "Đặt ngay";
    } else {
        // Nếu chưa chọn thì làm mờ nút, không cho bấm
        btnBookNow.setAttribute('href', '#');
        btnBookNow.classList.add('disabled');
        btnBookNow.style.opacity = "0.5";
        btnBookNow.style.pointerEvents = "none";
        btnBookNow.innerText = "Vui lòng chọn lịch trình";
    }

    // Tính toán lại Tổng Giá dựa trên số lượng khách và giá cơ bản (basePrice)
    if (widgetTotalPrice) {
        if (typeof basePrice !== 'undefined' && basePrice > 0) {
            const formattedTotal = (basePrice * quantity).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".") + '₫';
            widgetTotalPrice.innerText = formattedTotal;
        } else {
            widgetTotalPrice.innerText = 'Liên hệ';
        }
    }
};

// Gắn sự kiện (Event Listener) khi trang web tải xong
document.addEventListener('DOMContentLoaded', function () {
    btnMinus = document.getElementById('btn-minus');
    btnPlus = document.getElementById('btn-plus');
    inputQuantity = document.getElementById('guest-quantity');
    btnBookNow = document.getElementById('btn-book-now');
    widgetTotalPrice = document.getElementById('widget-total-price');

    if (btnMinus && btnPlus && inputQuantity && btnBookNow) {
        // Nút Giảm số lượng khách
        btnMinus.addEventListener('click', function () {
            let val = parseInt(inputQuantity.value);
            if (val > 1) {
                inputQuantity.value = val - 1;
                const warningEl = document.getElementById('capacity-warning');
                if (warningEl) warningEl.style.display = 'none'; // Ẩn cảnh báo khi giảm
                window.updateBookUrl(); // Cập nhật lại Link và Tổng giá
            }
        });

        // Nút Tăng số lượng khách
        btnPlus.addEventListener('click', function () {
            let val = parseInt(inputQuantity.value);
            let maxAllowed = currentAvailable !== null ? currentAvailable : 50;

            if (val >= maxAllowed) {
                if (currentAvailable !== null) {
                    const warningEl = document.getElementById('capacity-warning');
                    if (warningEl) {
                        document.getElementById('warning-slots').innerText = currentAvailable;
                        warningEl.style.display = 'block';
                    }
                }
                return; // Ngăn không cho tăng thêm
            }

            inputQuantity.value = val + 1;
            window.updateBookUrl(); // Cập nhật lại Link và Tổng giá
        });

        // Gọi lần đầu để setup trạng thái nút
        window.updateBookUrl();
    }
});
