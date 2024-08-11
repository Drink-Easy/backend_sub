package com.drinkeg.drinkeg.dto.securityDTO.oauth2DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String username;
    private String role;
    private String accessToken;
    // private String refreshToken
}
