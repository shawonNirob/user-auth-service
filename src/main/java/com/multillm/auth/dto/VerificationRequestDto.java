package com.multillm.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerificationRequestDto {
    @NotBlank(message = "Verification token cannot be blank")
    private String token;
}
