package com.multillm.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamInvitationDto {
    private Long id;
    private Long accountId;
    private String email;
    private String role;
    private String token;
    private boolean accepted;
}
