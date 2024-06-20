package com.godeltech.kafkaconsumer.repository;

import com.godeltech.kafkaconsumer.entity.Client;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends ListCrudRepository<Client, Long> {
}
