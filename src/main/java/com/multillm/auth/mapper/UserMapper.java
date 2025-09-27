package com.multillm.auth.mapper;


import com.multillm.auth.dto.UserDto;
import com.multillm.auth.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}
