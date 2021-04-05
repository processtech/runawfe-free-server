package ru.runa.wfe.presentation.filter;

import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

public class ChatRoomFilterCriteria extends FilterCriteria {
    private final Long id;

    public ChatRoomFilterCriteria(Long id) {
        super(1);
        this.id = id;
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        return "instance.executorId = " + id;
    }
}
