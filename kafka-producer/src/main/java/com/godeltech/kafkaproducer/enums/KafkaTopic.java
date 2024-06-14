package com.godeltech.kafkaproducer.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum KafkaTopic {
    CLIENT("client-name"), TRANSACTION("transaction-name");

    private final String name;
}
