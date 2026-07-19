package com.group3.hotel.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUserUpsertRequest {

    private Long id;

    private String email;

    private String password;

    private String fullName;

    private String phone;
}
