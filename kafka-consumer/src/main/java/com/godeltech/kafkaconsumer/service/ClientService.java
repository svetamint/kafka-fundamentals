package com.godeltech.kafkaconsumer.service;

import com.godeltech.kafkaconsumer.dto.ClientDto;
import com.godeltech.kafkaconsumer.entity.Client;
import com.godeltech.kafkaconsumer.mapper.ClientMapper;
import com.godeltech.kafkaconsumer.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public Client save(ClientDto clientDto) {
        log.info("Trying to store client with id: {}", clientDto.getClientId());
        return clientRepository.save(clientMapper.toClient(clientDto));
    }
}
