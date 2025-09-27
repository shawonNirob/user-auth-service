package com.multillm.auth.dto;

import com.multillm.auth.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateInvitationRequestDto {
    @NotBlank(message = "Email Cannot be blank")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotNull(message = "Role cannot be null")
    private Role role;
}
