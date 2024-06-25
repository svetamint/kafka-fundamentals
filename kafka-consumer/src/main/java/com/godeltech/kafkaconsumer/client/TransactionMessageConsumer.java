package com.godeltech.kafkaconsumer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godeltech.kafkaconsumer.dto.TransactionDto;
import com.godeltech.kafkaconsumer.service.DeadLetterQueueProcessor;
import com.godeltech.kafkaconsumer.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionMessageConsumer {
    @Value("${topics.transaction-name}")
    private String topic;
    private final KafkaConsumer<Long, String> transactionConsumer;
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    private final DeadLetterQueueProcessor deadLetterQueueProcessor;

    public void processTransaction() {
        try {
            transactionConsumer.subscribe(List.of(topic));
            while (true) {
                try {
                    transactionConsumer.poll(Duration.ofSeconds(10))
                            .forEach(record -> {
                                try {
                                    transactionService.save(toTransactionDto(record.value()));
                                } catch (Exception e) {
                                    log.error("Error processing record: {}, sending to DLQ", record, e);
                                    deadLetterQueueProcessor.sendToDlq(record, e);
                                }
                            });
                } catch (Exception exception) {
                    log.error("Unexpected error: {}", exception.getMessage());
                }
            }
        } finally {
            transactionConsumer.close();
        }
    }

    @SneakyThrows
    private TransactionDto toTransactionDto(String value) {
        return objectMapper.readValue(value, TransactionDto.class);
    }

}
