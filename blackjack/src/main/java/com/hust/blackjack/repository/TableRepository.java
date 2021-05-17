package com.hust.blackjack.repository;

import com.hust.blackjack.model.Table;

import java.util.Optional;

public interface TableRepository {
    Table findAvailableTable();

    Optional<Table> findTableById(String tableId);
}
