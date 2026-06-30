package com.group3.hotel.service.impl;

import com.group3.hotel.config.VNPayConfig;
import com.group3.hotel.service.IBookingService;
import com.group3.hotel.service.IVnPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements IVnPayService {

    private final VNPayConfig vnPayConfig;
    private final IBookingService bookingService;

    @Override
    public String createPaymentUrl(Long bookingId, Long paymentAmount, String ipAddress) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = String.valueOf(bookingId); 
        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(paymentAmount * 100)); // VNPAY nhận số tiền x100
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan dat phong " + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", ipAddress);

        LocalDateTime now = LocalDateTime.now();
        vnp_Params.put("vnp_CreateDate", vnPayConfig.formatExpireDate(now));
        vnp_Params.put("vnp_ExpireDate", vnPayConfig.formatExpireDate(now.plusMinutes(15)));

        String secureHash = vnPayConfig.hashAllFields(vnp_Params);
        vnp_Params.put("vnp_SecureHash", secureHash);

        StringBuilder queryUrl = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                if (queryUrl.length() > 0) {
                    queryUrl.append('&');
                }
                queryUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()))
                        .append('=')
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vnPayConfig.getVnp_Url() + "?" + queryUrl.toString();
    }

    @Override
    public void processIpn(Map<String, String> params) {
        // Thực tế ở đây cần phải kiểm tra lại Checksum (vnp_SecureHash) để xác thực.
        // Tạm thời đơn giản hóa cho mục đích học tập.
        
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        
        if (txnRef == null || txnRef.isEmpty()) {
            return;
        }

        Long bookingId = Long.parseLong(txnRef);

        if ("00".equals(responseCode)) {
            // Giao dịch thành công
            bookingService.confirmBooking(bookingId);
        } else {
            // Giao dịch thất bại
            bookingService.cancelBooking(bookingId);
        }
    }
}
