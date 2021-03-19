package ru.runa.wfe.presentation.filter;

import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

public class ChatRoomFilterCriteria extends FilterCriteria {

    private final Long id;

    public ChatRoomFilterCriteria(Long id) {
        this.id = id;
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        return "executor_id = " + id;
    }
}
