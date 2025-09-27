package com.multillm.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcceptInvitationRequestDto {
    @NotBlank(message = "Invitation token cannot be blank")
    private String invitationToken;
}
