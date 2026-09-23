package ru.runa.wfe.office.storage.binding;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CompositeQueryProperties {
    private final QueryProperties trigger;
    private final List<QueryProperties> subqueries;
}
