package ru.runa.wfe.office.storage.binding;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueryRole {
    TRIGGER(null),
    EXISTS("EXISTS"),
    NOT_EXISTS("NOT EXISTS");
    private final String sqlOperator;
}
