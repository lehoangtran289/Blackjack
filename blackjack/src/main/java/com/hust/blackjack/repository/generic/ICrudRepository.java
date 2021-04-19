package com.hust.blackjack.repository.generic;

import java.util.List;
import java.util.Optional;

public interface ICrudRepository<T, H> {
    List<T> search(T t);

    Optional<T> findById(H id);

    T update(T t);

    T save(T t);

    void delete(H id);
}
