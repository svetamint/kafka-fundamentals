package com.godeltech.kafkaconsumer.repository;

import com.godeltech.kafkaconsumer.entity.Transaction;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends ListCrudRepository<Transaction, Long> {
}
