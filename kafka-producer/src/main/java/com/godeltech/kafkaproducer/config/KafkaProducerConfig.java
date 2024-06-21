package com.godeltech.kafkaproducer.config;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.apache.kafka.clients.producer.ProducerConfig.*;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public KafkaProducer<Long, String> kafkaProducer() {
        return new KafkaProducer<>(getProperties());
    }

    private Properties getProperties() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ACKS_CONFIG, "all");
        return props;
    }

    @ConfigurationProperties(prefix = "topics")
    @Bean
    public Map<String, String> topicNames() {
        return new HashMap<>();
    }

}
