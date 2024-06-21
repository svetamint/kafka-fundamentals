package com.godeltech.config;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Locale;

@Configuration
public class KafkaTestContainersConfiguration {

    @Bean
    public TestRestTemplate restTemplate() {
        return new TestRestTemplate();
    }

    @Bean
    @Primary
    public Locale defaultLocale() {
        Locale.setDefault(Locale.ENGLISH);
        return Locale.ENGLISH;
    }


}
