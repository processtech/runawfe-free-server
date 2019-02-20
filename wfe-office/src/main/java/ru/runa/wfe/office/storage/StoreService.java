package ru.runa.wfe.office.storage;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.var.dto.WfVariable;

public interface StoreService {

    String PROP_CONSTRAINTS = "constraints";
    String PROP_PATH = "path";
    String PROP_FORMAT = "format";

    void createFileIfNotExist(String path) throws Exception;

    ExecutionResult findByFilter(Properties properties, WfVariable variable, String condition) throws Exception;

    void update(Properties properties, WfVariable variable, String condition) throws Exception;

    void delete(Properties properties, WfVariable variable, String condition) throws Exception;

    void save(Properties properties, WfVariable variable, boolean appendTo) throws Exception;

    List<String> operators = Lists.newArrayList("==", "!=", ">=", "<=", ">", "<", "like", "and", "or");

    default boolean isConditionValid(String condition) {
        if (!Strings.isNullOrEmpty(condition)) {
            Set<Integer> indexes = Sets.newHashSet();
            condition = condition.toLowerCase();
            for (String operator : operators) {
                int index = -1;
                while ((index = condition.indexOf(operator, index + 1)) > 0) {
                    if (!indexes.contains(index) && (condition.charAt(index - 1) != ' '
                            || index < condition.length() - operator.length() && condition.charAt(index + operator.length()) != ' ')) {
                        return false;
                    }
                    indexes.add(index);
                }
            }
        }
        return true;
    }

}
