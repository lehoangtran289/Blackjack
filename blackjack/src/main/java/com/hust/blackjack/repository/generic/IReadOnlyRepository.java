package com.hust.blackjack.repository.generic;

import java.util.List;
import java.util.Optional;

public interface IReadOnlyRepository<T, H> {
    List<T> search(T t);

    Optional<T> findById(H id);
}
