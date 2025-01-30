package org.acme.mappers;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.dto.requests.JoinRequestDto;
import org.acme.model.requests.JoinRequest;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;


public interface JoinRequestMapper {

    JoinRequestMapper INSTANCE = Mappers.getMapper(JoinRequestMapper.class);

    JoinRequestDto entityToDto(JoinRequest entity);

    JoinRequest dtoToEntity(JoinRequestDto dto);
}
