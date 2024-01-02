package ru.runa.wfe.execution.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import java.text.ParseException;
import java.util.Map;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.QCurrentToken;
import ru.runa.wfe.execution.process.check.FrozenProcessFilter;
import ru.runa.wfe.lang.NodeType;

@Component
public class FrozenTokenFilterHandler {
    public static final String DATE_DELIMITER = "##";

    public BooleanExpression getExpression(BooleanExpression expression, Map<FrozenProcessFilter, String> filters) {
            QCurrentToken t = QCurrentToken.currentToken;
            for (Map.Entry<FrozenProcessFilter, String> entry : filters.entrySet()) {
                switch (entry.getKey()) {
                case PROCESS_NAME:
                    StringPath processName = t.process.definition.pack.name;
                    String processNameFilter = entry.getValue();
                    expression = expression.and(processName.likeIgnoreCase("%" + processNameFilter.replace("*", "%") + "%"));
                    break;
                case NODE_TYPE:
                    NodeType filterNodeType = NodeType.valueOf(entry.getValue());
                    expression = expression.and(t.nodeType.eq(filterNodeType));
                    break;
                case PROCESS_VERSION:
                    NumberPath<Long> version = t.process.definition.version;
                    Long filterVersion = Long.parseLong(entry.getValue());
                    expression = expression.and(version.eq(filterVersion));
                    break;
                case NODE_ENTER_DATE:
                    String[] dates = entry.getValue().split(DATE_DELIMITER);
                    if (dates.length == 0) {
                        break;
                    }
                    String firstDate = dates[0];
                    String secondDate = dates.length > 1 ? dates[1] : "";
                    String dateTemplate = "dd.MM.yyyy HH:mm";
                    try {
                        if (secondDate.length() == 0) {
                            expression = expression.and(t.nodeEnterDate.after(DateUtils.parseDate(firstDate, dateTemplate)));
                        } else if (firstDate.length() == 0) {
                            expression = expression.and(t.nodeEnterDate.before(DateUtils.parseDate(secondDate, dateTemplate)));
                        } else {
                            expression = expression.and(t.nodeEnterDate.after(DateUtils.parseDate(firstDate, dateTemplate)))
                                        .and(t.nodeEnterDate.before(DateUtils.parseDate(secondDate, dateTemplate)));
                        }
                    } catch (ParseException e) {
                        throw new InternalApplicationException(e);
                    }
                    break;
                default:
                    throw new InternalApplicationException(String.format("Frozen token filter name %s does not match any in %s logic",
                            entry.getKey().getName(), this.getClass().getName()));
            }
        }
        return expression;
    }
}
