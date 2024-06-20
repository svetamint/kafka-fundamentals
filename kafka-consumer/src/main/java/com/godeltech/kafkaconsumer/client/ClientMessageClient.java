package com.godeltech.kafkaconsumer.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.godeltech.kafkaconsumer.dto.ClientDto;
import com.godeltech.kafkaconsumer.service.ClientService;
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
public class ClientMessageClient {
    @Value("${topics.client-name}")
    private String topic;
    private final KafkaConsumer<Long, String> clientConsumer;
    private final ObjectMapper objectMapper;
    private final ClientService clientService;

    public void processClient() {
        try {
            clientConsumer.subscribe(List.of(topic));
            while (true) {
                clientConsumer.poll(Duration.ofSeconds(10))
                        .forEach(record -> clientService.save(toClientDto(record.value())));
            }
        } catch (Exception exception) {
            log.error("Unexpected error: {}", exception.getMessage());
        } finally {
            clientConsumer.close();
        }
    }

    @SneakyThrows
    private ClientDto toClientDto(String value) {
        return objectMapper.readValue(value, ClientDto.class);
    }
}
