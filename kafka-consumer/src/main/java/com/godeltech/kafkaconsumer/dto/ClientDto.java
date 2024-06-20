package com.godeltech.kafkaconsumer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDto {
    @NotNull
    private Long clientId;

    @Email
    @NotNull
    private String email;
}
