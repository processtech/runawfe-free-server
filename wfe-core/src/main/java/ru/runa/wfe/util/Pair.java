package ru.runa.wfe.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Pair<T1, T2> {
    private T1 value1;
    private T2 value2;
}
