package com.multillm.auth.mapper;

import com.multillm.auth.dto.AccountDto;
import com.multillm.auth.dto.CreateTeamRequestDto;
import com.multillm.auth.model.Account;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDto toDto(Account account);
    Account toAccount(CreateTeamRequestDto accountDto);
}
