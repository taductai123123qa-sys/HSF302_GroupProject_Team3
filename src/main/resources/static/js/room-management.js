// =======================================================
// MODULE 2: AJAX ENGINE CHO CẬP NHẬT TRẠNG THÁI PHÒNG
// =======================================================
window.changeRoomStatusViaAjax = function(buttonElement, targetStatus) {
    // 1. Thu thập Token Bảo mật (CSRF)
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    
    let csrfToken = '';
    let csrfHeaderName = 'X-CSRF-TOKEN';
    
    if (csrfTokenMeta && csrfHeaderMeta) {
        csrfToken = csrfTokenMeta.getAttribute('content');
        csrfHeaderName = csrfHeaderMeta.getAttribute('content');
    }

    // 2. Thu thập Room ID từ thẻ cha gần nhất
    const roomCard = buttonElement.closest('.room-card');
    if (!roomCard) return;
    
    const roomId = roomCard.getAttribute('data-id');
    if (!roomId) {
        alert("Lỗi không tìm thấy ID phòng.");
        return;
    }

    // Khởi tạo headers với CSRF Token
    const headers = {
        'Content-Type': 'application/json'
    };
    if (csrfToken) {
        headers[csrfHeaderName] = csrfToken;
    }

    // 3. Cấu hình Fetch API (PATCH)
    fetch(`/api/v1/rooms/${roomId}/status`, {
        method: 'PATCH',
        headers: headers,
        body: JSON.stringify({ newStatus: targetStatus })
    })
    .then(response => {
        // Bắt mã lỗi 409 Xung đột
        if (response.status === 409) {
            alert("❌ XUNG ĐỘT DỮ LIỆU: Phòng này vừa bị một nhân viên khác hoặc hệ thống thay đổi trạng thái trước đó. Trình duyệt sẽ tự động làm mới (F5) sơ đồ.");
            window.location.reload();
            return Promise.reject("409_CONFLICT");
        }
        return response.json();
    })
    .then(data => {
        // 4. Xử lý kết quả trả về
        if (data.success === true) {
            // Happy Path: Xử lý DOM
            // Cập nhật data-status
            roomCard.setAttribute('data-status', targetStatus);

            // Xóa các class cũ bắt đầu bằng 'status-'
            roomCard.classList.forEach(cls => {
                if (cls.startsWith('status-')) {
                    roomCard.classList.remove(cls);
                }
            });
            // Thêm class mới
            roomCard.classList.add('status-' + targetStatus.toLowerCase());

            // Đổi nhãn
            const badge = roomCard.querySelector('.room-status-badge');
            if (badge) {
                badge.innerText = targetStatus.toUpperCase();
            }
        } else {
            // Vi phạm nghiệp vụ
            alert(data.message || "Lỗi nghiệp vụ. Không thể thực hiện cập nhật!");
        }
    })
    .catch(error => {
        if (error !== "409_CONFLICT") {
            console.error("Error updating room status:", error);
            alert("Lỗi hệ thống khi gọi API cập nhật trạng thái!");
        }
    });
};
