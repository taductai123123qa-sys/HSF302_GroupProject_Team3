package com.group3.hotel.service.impl;

import com.group3.hotel.entity.Customer;
import com.group3.hotel.repository.CustomerRepository;
import com.group3.hotel.service.ICustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerServiceImpl implements ICustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public Optional<Customer> findByUserEmail(String email) {
        return customerRepository.findByUserEmail(email);
    }
}
