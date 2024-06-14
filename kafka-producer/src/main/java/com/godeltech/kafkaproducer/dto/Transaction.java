package com.godeltech.kafkaproducer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.godeltech.kafkaproducer.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @NotNull
    private String bank;

    @NotNull
    private Long clientId;

    @NotNull
    private TransactionType orderType;

    @NotNull
    private Integer quantity;

    @NotNull
    private Double price;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
