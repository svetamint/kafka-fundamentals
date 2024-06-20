package com.godeltech.kafkaconsumer.mapper;

import com.godeltech.kafkaconsumer.dto.ClientDto;
import com.godeltech.kafkaconsumer.entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    @Mapping(target = "id", source = "clientId")
    Client toClient(ClientDto dto);
}
