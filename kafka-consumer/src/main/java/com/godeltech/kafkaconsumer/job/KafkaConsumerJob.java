package com.godeltech.kafkaconsumer.job;

import com.godeltech.kafkaconsumer.client.ClientMessageClient;
import com.godeltech.kafkaconsumer.client.TransactionMessageClient;
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

    private final ClientMessageClient clientMessageClient;
    private final TransactionMessageClient transactionMessageClient;

    @Scheduled(initialDelay = 500, fixedDelay = Long.MAX_VALUE)
    public void processMessages() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(clientMessageClient::processClient);
        executorService.submit(transactionMessageClient::processTransaction);
    }
}
