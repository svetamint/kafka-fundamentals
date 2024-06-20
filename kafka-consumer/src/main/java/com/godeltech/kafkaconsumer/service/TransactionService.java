package com.godeltech.kafkaconsumer.service;

import com.godeltech.kafkaconsumer.dto.TransactionDto;
import com.godeltech.kafkaconsumer.entity.Client;
import com.godeltech.kafkaconsumer.entity.Transaction;
import com.godeltech.kafkaconsumer.mapper.TransactionMapper;
import com.godeltech.kafkaconsumer.repository.ClientRepository;
import com.godeltech.kafkaconsumer.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private static final String DUMMY_EMAIL = "dummy@email.com";
    private final TransactionMapper transactionMapper;
    private final ClientRepository clientRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public Transaction save(TransactionDto transactionDto) {
        Long clientId = transactionDto.getClientId();
        log.info("Trying to store transaction with client id = {}", clientId);
        if(!clientRepository.existsById(clientId)) {
            clientRepository.save(dummyClient(clientId));
        }
        return transactionRepository.save(transactionMapper.toTransaction(transactionDto));
    }

    private Client dummyClient(Long id) {
        return Client.builder()
                .id(id)
                .email(DUMMY_EMAIL)
                .build();
    }
}
