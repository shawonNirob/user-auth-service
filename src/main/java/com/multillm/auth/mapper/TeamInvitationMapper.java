package com.multillm.auth.mapper;

import com.multillm.auth.dto.TeamInvitationDto;
import com.multillm.auth.model.TeamInvitation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TeamInvitationMapper {
    TeamInvitationDto toDto(TeamInvitation teamInvitation);
    TeamInvitation toTeamInvitation(TeamInvitationDto teamInvitationDto);
}
