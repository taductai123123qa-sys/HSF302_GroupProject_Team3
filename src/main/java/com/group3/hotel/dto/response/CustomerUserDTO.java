package com.group3.hotel.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUserDTO {

    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
}
