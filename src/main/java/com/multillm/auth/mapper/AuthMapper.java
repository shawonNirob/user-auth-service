package com.multillm.auth.mapper;

import com.multillm.auth.dto.RegisterRequestDto;
import com.multillm.auth.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    User toUser(RegisterRequestDto registerRequestDto);
}

