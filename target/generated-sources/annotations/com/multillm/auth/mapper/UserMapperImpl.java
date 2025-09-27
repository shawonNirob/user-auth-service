package com.multillm.auth.mapper;

import com.multillm.auth.dto.UserDto;
import com.multillm.auth.model.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-27T13:11:14+0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        if ( user.getId() != null ) {
            userDto.id( user.getId() );
        }
        userDto.name( user.getName() );
        userDto.email( user.getEmail() );
        userDto.avatarUrl( user.getAvatarUrl() );

        return userDto.build();
    }

    @Override
    public User toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userDto.getId() );
        user.email( userDto.getEmail() );
        user.name( userDto.getName() );
        user.avatarUrl( userDto.getAvatarUrl() );

        return user.build();
    }
}
