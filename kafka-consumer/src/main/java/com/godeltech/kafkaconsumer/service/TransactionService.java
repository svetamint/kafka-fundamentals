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
        Transaction transaction = transactionMapper.toTransaction(transactionDto);
        transaction.setClient(getClient(clientId));
        return transactionRepository.save(transaction);
    }

    private Client getClient(Long id) {
        Client dummyClient = Client.builder()
                .id(id)
                .email(DUMMY_EMAIL)
                .build();
        return clientRepository.findById(id)
                .orElseGet(() -> clientRepository.save(dummyClient));
    }
}
