package com.hust.blackjack.common.tuple;

import com.hust.blackjack.common.Assertions;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class Tuple3<A0, A1, A2> implements Tuple {
    private final A0 a0;
    private final A1 a1;
    private final A2 a2;

    @Override
    public Object get(int index) {
        Assertions.inRangeChecks(index, 0, 3);
        switch (index) {
            case 0:
                return a0;
            case 1:
                return a1;
            case 2:
                return a2;
            default:
                throw new ArrayIndexOutOfBoundsException();
        }
    }

    public static <AS0, AS1, AS2> Tuple3<AS0, AS1, AS2> of(AS0 arg0, AS1 arg1, AS2 arg2) {
        return new Tuple3<>(arg0, arg1, arg2);
    }
}
