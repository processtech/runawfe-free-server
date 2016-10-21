package ru.runa.wfe.extension.handler.var;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.extension.handler.CommonHandler;
import ru.runa.wfe.extension.handler.var.ConvertMapsToListsConfig.Sorting;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ConvertMapsToListsHandler extends CommonHandler {
    private ConvertMapsToListsConfig config;

    @Override
    public void setConfiguration(String configuration) {
        config = ConvertMapsToListsConfig.fromXml(configuration);
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        Map<String, Object> output = Maps.newHashMap();
        String sortBy = config.getSorting().getSortBy();
        List<SortingKey> sortingKeys = Lists.newArrayList();
        if (Sorting.NONE.equals(sortBy) || Sorting.KEYS.equals(sortBy)) {
            Map<Comparable, Object> map = variableProvider.getValueNotNull(Map.class, config.getOperations().get(0).getMapVariableName());
            for (Map.Entry<Comparable, Object> entry : map.entrySet()) {
                sortingKeys.add(new SortingKey(entry.getKey(), entry.getKey()));
            }
        } else {
            int opIndex = Integer.parseInt(sortBy.substring(Sorting.VALUES.length()));
            Map<Object, Comparable> map = variableProvider.getValueNotNull(Map.class, config.getOperations().get(opIndex).getMapVariableName());
            for (Map.Entry<Object, Comparable> entry : map.entrySet()) {
                sortingKeys.add(new SortingKey(entry.getValue(), entry.getKey()));
            }
        }
        Collections.sort(sortingKeys);
        if (Sorting.MODE_DESC.equals(config.getSorting().getSortMode())) {
            Collections.reverse(sortingKeys);
        }
        for (ConvertMapToListOperation operation : config.getOperations()) {
            Map map = variableProvider.getValueNotNull(Map.class, operation.getMapVariableName());
            List list = Lists.newArrayListWithExpectedSize(sortingKeys.size());
            for (SortingKey sortingKey : sortingKeys) {
                list.add(map.get(sortingKey.key));
            }
            output.put(operation.getListVariableName(), list);
        }
        return output;
    }

    private class SortingKey implements Comparable<SortingKey> {
        private final Comparable sortedValue;
        private final Object key;

        public SortingKey(Comparable sortedValue, Object key) {
            this.sortedValue = sortedValue;
            this.key = key;
        }

        @Override
        public int compareTo(SortingKey o) {
            if (sortedValue == null) {
                return -1;
            }
            return sortedValue.compareTo(o.sortedValue);
        }
    }

}
