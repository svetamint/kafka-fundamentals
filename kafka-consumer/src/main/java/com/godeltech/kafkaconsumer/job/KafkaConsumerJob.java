package com.godeltech.kafkaconsumer.job;

import com.godeltech.kafkaconsumer.client.ClientMessageConsumer;
import com.godeltech.kafkaconsumer.client.TransactionMessageConsumer;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@AllArgsConstructor
@Profile("!test")
public class KafkaConsumerJob {

    private final ClientMessageConsumer clientMessageConsumer;
    private final TransactionMessageConsumer transactionMessageConsumer;

    @Scheduled(initialDelay = 500, fixedDelay = Long.MAX_VALUE)
    public void processMessages() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(clientMessageConsumer::processClient);
        executorService.submit(transactionMessageConsumer::processTransaction);
    }
}
