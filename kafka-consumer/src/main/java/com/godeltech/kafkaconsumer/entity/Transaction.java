package com.godeltech.kafkaconsumer.entity;

import com.godeltech.kafkaconsumer.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bank;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type")
    private TransactionType orderType;

    private Integer quantity;

    private Double price;

    private Double cost;

    @Column(name = "create_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name="client_id", nullable = false)
    private Client client;
}
