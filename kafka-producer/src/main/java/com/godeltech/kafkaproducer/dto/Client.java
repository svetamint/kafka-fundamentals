package com.godeltech.kafkaproducer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @NotNull
    private Long clientId;

    @Email
    @NotNull
    private String email;
}
