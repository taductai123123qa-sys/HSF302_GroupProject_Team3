package com.group3.hotel.service;

import com.group3.hotel.entity.Customer;
import java.util.Optional;

public interface ICustomerService {
    Optional<Customer> findByUserEmail(String email);
}
