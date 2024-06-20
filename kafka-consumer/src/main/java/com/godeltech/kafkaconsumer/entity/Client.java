package com.godeltech.kafkaconsumer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "clients")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Client {
    @Id
    private Long id;

    private String email;

    @OneToMany(mappedBy = "client")
    private List<Transaction> transactions;
}
