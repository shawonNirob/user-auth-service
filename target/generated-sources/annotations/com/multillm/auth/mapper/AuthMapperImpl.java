package com.multillm.auth.mapper;

import com.multillm.auth.dto.RegisterRequestDto;
import com.multillm.auth.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-27T13:11:14+0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Eclipse Adoptium)"
)
@Component
public class AuthMapperImpl implements AuthMapper {

    @Override
    public User toUser(RegisterRequestDto registerRequestDto) {
        if ( registerRequestDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( registerRequestDto.getEmail() );
        user.name( registerRequestDto.getName() );

        user.emailVerified( false );

        return user.build();
    }
}
