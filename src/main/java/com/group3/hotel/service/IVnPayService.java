package com.group3.hotel.service;

import java.math.BigDecimal;
import java.util.Map;

public interface IVnPayService {

    /**
     * Tạo URL thanh toán VNPay.
     * @param bookingId Mã đơn đặt phòng
     * @param paymentAmount Số tiền cần thanh toán
     * @param ipAddress IP của khách hàng
     * @return Chuỗi URL redirect sang VNPay
     */
    String createPaymentUrl(Long bookingId, Long paymentAmount, String ipAddress);

    /**
     * Xử lý IPN từ VNPay trả về để cập nhật trạng thái đơn hàng.
     * @param params Các tham số VNPay trả về
     */
    void processIpn(Map<String, String> params);
}
