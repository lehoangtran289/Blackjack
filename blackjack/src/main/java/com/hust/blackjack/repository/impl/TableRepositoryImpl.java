package com.hust.blackjack.repository.impl;

import com.hust.blackjack.exception.TableException;
import com.hust.blackjack.model.Table;
import com.hust.blackjack.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TableRepositoryImpl implements TableRepository {
    private List<Table> tables;

    @PostConstruct
    public void init() {
        tables = new ArrayList<>();
        tables.add(new Table(1)); // init 1 table in system
    }

    @Override
    public Table findAvailableTable() {
        for (Table table : tables) {
            if (table.getPlayers().size() < Table.TABLE_SIZE) {
                return table;
            }
        }
        Table newTable = new Table(tables.size() + 1);
        tables.add(newTable);
        return newTable;
    }

    @Override
    public Optional<Table> findTableById(int tableId) {
        return tables.stream()
                        .filter(table -> tableId == table.getTableId())
                        .findFirst();
    }
}
