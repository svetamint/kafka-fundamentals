package com.godeltech.kafkaconsumer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.godeltech.kafkaconsumer.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
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
