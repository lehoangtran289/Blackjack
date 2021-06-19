package com.hust.blackjack.repository.impl;

import com.hust.blackjack.common.RandomId;
import com.hust.blackjack.model.Table;
import com.hust.blackjack.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
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
        tables.add(new Table(RandomId.generate())); // init 1 table in system
    }

    /**
     * @return available table which 1. has no password + allow free join; 2. is not playing
     */
    @Override
    public Table findAvailableTable() {
        for (Table table : tables) {
            if (table.getIsAllowFreeJoin() == 1 && StringUtils.isEmpty(table.getPassword()) &&
                    table.getIsPlaying() == 0 && table.getPlayers().size() < Table.TABLE_SIZE) {
                return table;
            }
        }
        Table newTable = new Table(RandomId.generate());
        tables.add(newTable);
        return newTable;
    }

    @Override
    public Table createPrivateTable(String password) {
        Table newTable = new Table(RandomId.generate(), password);
        tables.add(newTable);
        return newTable;
    }

    @Override
    public Optional<Table> findTableById(String tableId) {
        return tables.stream()
                .filter(table -> StringUtils.equals(table.getTableId(), tableId))
                .findFirst();
    }
}
