package com.godeltech.kafkaproducer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godeltech.kafkaproducer.dto.Transaction;
import com.godeltech.kafkaproducer.enums.KafkaTopic;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final KafkaProducer<Long, String> kafkaProducer;
    private final ObjectMapper objectMapper;
    private final Map<String, String> topicNames;

    @PostMapping
    @SneakyThrows
    public void createTransaction(@RequestBody @Valid Transaction transaction) {
        log.info("creating transaction: {}", transaction);
        ProducerRecord<Long, String> producerRecord = new ProducerRecord<>(topicNames.get(KafkaTopic.TRANSACTION.getName()),
                transaction.getClientId(),
                objectMapper.writeValueAsString(transaction));
        kafkaProducer.send(producerRecord);
    }

}
