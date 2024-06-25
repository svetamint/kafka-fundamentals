package com.godeltech.kafkaconsumer.mapper;

import com.godeltech.kafkaconsumer.dto.TransactionDto;
import com.godeltech.kafkaconsumer.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "cost", source = "dto", qualifiedByName = "transactionCost")
    @Mapping(target = "client.id", source = "dto.clientId")
    Transaction toTransaction(TransactionDto dto);

    @Named("transactionCost")
    default Double calculateCost(TransactionDto dto) {
        return dto.getPrice() * dto.getQuantity();
    }
}
