package com.hust.blackjack.repository;

import com.hust.blackjack.model.Table;

public interface TableRepository {
    Table findAvailableTable();
}
