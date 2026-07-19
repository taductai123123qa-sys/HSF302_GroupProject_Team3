package com.group3.hotel.service;

import com.group3.hotel.dto.request.CustomerUserUpsertRequest;

import java.util.List;

public interface AdminCustomerService {

    List<com.group3.hotel.dto.response.CustomerUserDTO> getAllCustomers();

    com.group3.hotel.dto.response.CustomerUserDTO getCustomerById(Long id);

    /**
     * Tạo mới hoặc cập nhật Customer User.
     * - Nếu id == null -> tạo mới User + Customer
     * - Nếu id != null -> cập nhật thông tin Customer và User (email/fullName)
     * - Nếu password không rỗng -> cập nhật lại mật khẩu (mã hoá BCrypt)
     */
    void saveCustomer(CustomerUserUpsertRequest request);

    void deleteCustomer(Long id);
}
