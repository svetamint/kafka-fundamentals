package com.godeltech.kafkaproducer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godeltech.kafkaproducer.dto.Client;
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
@RequestMapping("/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {
    private final KafkaProducer<Long, String> kafkaProducer;
    private final ObjectMapper objectMapper;
    private final Map<String, String> topicNames;

    @PostMapping
    @SneakyThrows
    public void createClient(@RequestBody @Valid Client client) {
        log.info("creating client: {}", client);
        ProducerRecord<Long, String> producerRecord = new ProducerRecord<>(topicNames.get(KafkaTopic.CLIENT.getName()),
                client.getClientId(),
                objectMapper.writeValueAsString(client)
        );
        kafkaProducer.send(producerRecord);
    }
}
