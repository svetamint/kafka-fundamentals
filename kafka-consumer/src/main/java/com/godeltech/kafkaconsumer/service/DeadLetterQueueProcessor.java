package com.godeltech.kafkaconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.godeltech.kafkaconsumer.dto.DLQMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeadLetterQueueProcessor {
    @Value("${topics.dlq-name}")
    private String dlqTopic;
    private final KafkaProducer<Long, String> deadLetterQueueProducer;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void sendToDlq(ConsumerRecord<Long, String> record, Exception e) {
        DLQMessage dlQmessage = createDLQmessage(record, e);
        ProducerRecord<Long, String> failedMessage = new ProducerRecord<>(dlqTopic, objectMapper.writeValueAsString(dlQmessage));
        deadLetterQueueProducer.send(failedMessage);
    }

    private DLQMessage createDLQmessage(ConsumerRecord<Long, String> record, Exception e) {
        return DLQMessage.builder()
                .key(record.key())
                .offset(record.offset())
                .partition(record.partition())
                .topic(record.topic())
                .value(record.value())
                .errorMessage(e.getMessage())
                .build();
    }
}
