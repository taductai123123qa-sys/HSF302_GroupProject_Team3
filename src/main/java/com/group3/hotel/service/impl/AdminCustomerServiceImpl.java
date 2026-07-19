package com.group3.hotel.service.impl;

import com.group3.hotel.dto.request.CustomerUserUpsertRequest;
import com.group3.hotel.dto.response.CustomerUserDTO;
import com.group3.hotel.entity.Customer;
import com.group3.hotel.entity.User;
import com.group3.hotel.enums.UserRole;
import com.group3.hotel.exception.BadRequestException;
import com.group3.hotel.exception.ResourceNotFoundException;
import com.group3.hotel.repository.CustomerRepository;
import com.group3.hotel.repository.UserRepository;
import com.group3.hotel.service.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<CustomerUserDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .sorted((a, b) -> {
                    String nameA = a.getFullName() == null ? "" : a.getFullName();
                    String nameB = b.getFullName() == null ? "" : b.getFullName();
                    return nameA.compareToIgnoreCase(nameB);
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CustomerUserDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng id = " + id));
        return toDTO(customer);
    }

    @Override
    @Transactional
    public void saveCustomer(CustomerUserUpsertRequest request) {
        validate(request);

        if (request.getId() == null) {
            create(request);
        } else {
            update(request);
        }
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng id = " + id));

        // Ngăn xoá nếu khách đang có đơn đặt phòng liên quan
        if (customer.getBookings() != null && !customer.getBookings().isEmpty()) {
            throw new BadRequestException(
                    "Không thể xóa khách hàng '" + customer.getFullName()
                            + "' vì đang có đơn đặt phòng liên quan trong hệ thống.");
        }

        User user = customer.getUser();
        try {
            customerRepository.delete(customer);
            customerRepository.flush();
            if (user != null) {
                userRepository.delete(user);
                userRepository.flush();
            }
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException(
                    "Không thể xóa khách hàng vì còn dữ liệu liên quan (đơn đặt, thanh toán...).");
        }
    }

    private void create(CustomerUserUpsertRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BadRequestException("Email '" + req.getEmail() + "' đã được sử dụng.");
        }

        User user = User.builder()
                .email(req.getEmail().trim())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.GUEST)
                .build();
        user = userRepository.save(user);

        Customer customer = Customer.builder()
                .user(user)
                .fullName(req.getFullName().trim())
                .phone(req.getPhone() == null ? null : req.getPhone().trim())
                .build();
        customerRepository.save(customer);
    }

    private void update(CustomerUserUpsertRequest req) {
        Customer customer = customerRepository.findById(req.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng id = " + req.getId()));

        User user = customer.getUser();
        if (user == null) {
            throw new BadRequestException("Tài khoản đăng nhập của khách hàng này đã bị lỗi, không thể cập nhật.");
        }

        // Nếu đổi email, đảm bảo email mới không trùng với user khác
        if (!user.getEmail().equalsIgnoreCase(req.getEmail())) {
            Optional<User> exist = userRepository.findByEmail(req.getEmail());
            if (exist.isPresent() && !exist.get().getId().equals(user.getId())) {
                throw new BadRequestException("Email '" + req.getEmail() + "' đã được sử dụng bởi tài khoản khác.");
            }
            user.setEmail(req.getEmail().trim());
        }

        // Chỉ cập nhật mật khẩu khi người dùng nhập mới
        if (req.getPassword() != null && !req.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(user);

        customer.setFullName(req.getFullName().trim());
        customer.setPhone(req.getPhone() == null ? null : req.getPhone().trim());
        customerRepository.save(customer);
    }

    private void validate(CustomerUserUpsertRequest req) {
        if (req == null) {
            throw new BadRequestException("Dữ liệu không hợp lệ.");
        }
        if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email không được để trống.");
        }
        if (req.getFullName() == null || req.getFullName().trim().isEmpty()) {
            throw new BadRequestException("Họ tên không được để trống.");
        }
        if (req.getId() == null) {
            if (req.getPassword() == null || req.getPassword().trim().isEmpty()) {
                throw new BadRequestException("Mật khẩu không được để trống khi tạo mới.");
            }
            if (req.getPassword().length() < 6) {
                throw new BadRequestException("Mật khẩu phải có ít nhất 6 ký tự.");
            }
        } else if (req.getPassword() != null && !req.getPassword().isEmpty() && req.getPassword().length() < 6) {
            throw new BadRequestException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
    }

    private CustomerUserDTO toDTO(Customer customer) {
        User user = customer.getUser();
        return CustomerUserDTO.builder()
                .id(customer.getId())
                .userId(user != null ? user.getId() : null)
                .email(user != null ? user.getEmail() : "")
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .avatarUrl(customer.getAvatarUrl())
                .build();
    }
}
