package com.multillm.auth.mapper;

import com.multillm.auth.dto.AccountDto;
import com.multillm.auth.dto.CreateTeamRequestDto;
import com.multillm.auth.model.Account;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-27T13:11:14+0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Eclipse Adoptium)"
)
@Component
public class AccountMapperImpl implements AccountMapper {

    @Override
    public AccountDto toDto(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountDto.AccountDtoBuilder accountDto = AccountDto.builder();

        accountDto.id( account.getId() );
        accountDto.name( account.getName() );
        if ( account.getAccountType() != null ) {
            accountDto.accountType( account.getAccountType().name() );
        }

        return accountDto.build();
    }

    @Override
    public Account toAccount(CreateTeamRequestDto accountDto) {
        if ( accountDto == null ) {
            return null;
        }

        Account.AccountBuilder account = Account.builder();

        account.name( accountDto.getName() );

        return account.build();
    }
}
