package com.godeltech.kafkaconsumer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DLQMessage {
    private String topic;
    private int partition;
    private long offset;
    private Long key;
    private String value;
    private String errorMessage;
}
