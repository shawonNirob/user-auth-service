package com.multillm.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class AccountMemberId implements Serializable {

    @Column(name = "account_id")
    private Long accountId;

    @Column(name =  "user_id")
    private Long userId;

}
