package com.group3.hotel.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.group3.hotel.util.VNPayUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

@Configuration
@Getter
public class VNPayConfig {
    @Value("${vnpay.tmnCode:D6ELPNNF}")
    private String vnp_TmnCode;

    @Value("${vnpay.hashSecret:KPOPXEJ9RL0CSEL3IO6O0W24XO59PWPA}")
    private String vnp_HashSecret;

    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String vnp_Url;

    @Value("${vnpay.returnUrl:http://localhost:8080/bookings/vnpay-callback}")
    private String vnp_ReturnUrl;

    private static final DateTimeFormatter VNPAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    // Hàm sắp xếp và băm toàn bộ các tham số giao dịch để đối chiếu với chữ ký của VNPAY
    public String hashAllFields(Map<String, String> fields) {
        String hashData = fields.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    try {
                        String encodedName = java.net.URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString());
                        String encodedValue = java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString());
                        return encodedName + "=" + encodedValue;
                    } catch (java.io.UnsupportedEncodingException e) {
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));
                
        return VNPayUtil.hmacSHA512(vnp_HashSecret, hashData);
    }

    // Lấy IP của Client để gọi API VNPAY
    public String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }

    public String formatExpireDate(LocalDateTime dateTime) {
        return dateTime.format(VNPAY_DATE_FORMATTER);
    }
}
