package com.multillm.auth.mapper;

import com.multillm.auth.dto.TeamInvitationDto;
import com.multillm.auth.model.TeamInvitation;
import com.multillm.auth.model.enums.Role;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-27T13:11:14+0600",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.13 (Eclipse Adoptium)"
)
@Component
public class TeamInvitationMapperImpl implements TeamInvitationMapper {

    @Override
    public TeamInvitationDto toDto(TeamInvitation teamInvitation) {
        if ( teamInvitation == null ) {
            return null;
        }

        TeamInvitationDto.TeamInvitationDtoBuilder teamInvitationDto = TeamInvitationDto.builder();

        teamInvitationDto.id( teamInvitation.getId() );
        teamInvitationDto.email( teamInvitation.getEmail() );
        if ( teamInvitation.getRole() != null ) {
            teamInvitationDto.role( teamInvitation.getRole().name() );
        }
        teamInvitationDto.token( teamInvitation.getToken() );
        teamInvitationDto.accepted( teamInvitation.isAccepted() );

        return teamInvitationDto.build();
    }

    @Override
    public TeamInvitation toTeamInvitation(TeamInvitationDto teamInvitationDto) {
        if ( teamInvitationDto == null ) {
            return null;
        }

        TeamInvitation.TeamInvitationBuilder teamInvitation = TeamInvitation.builder();

        teamInvitation.id( teamInvitationDto.getId() );
        teamInvitation.email( teamInvitationDto.getEmail() );
        if ( teamInvitationDto.getRole() != null ) {
            teamInvitation.role( Enum.valueOf( Role.class, teamInvitationDto.getRole() ) );
        }
        teamInvitation.token( teamInvitationDto.getToken() );
        teamInvitation.accepted( teamInvitationDto.isAccepted() );

        return teamInvitation.build();
    }
}
