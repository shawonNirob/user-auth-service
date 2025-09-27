package com.multillm.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequestDto {

    @NotBlank(message = "Google ID token cannot be blank")
    private String idToken;
}
