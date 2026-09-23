package ru.runa.wfe.office.storage.handler;

import com.google.common.base.Preconditions;
import java.util.List;
import org.dom4j.Element;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.office.excel.ExcelConstraints;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.LogicalOperator;
import ru.runa.wfe.office.storage.binding.QueryRole;
import ru.runa.wfe.office.storage.binding.QueryType;

public class StorageBindingsParser extends FilesSupplierConfigParser<DataBindings> {

    @Override
    protected DataBindings instantiate() {
        return new DataBindings();
    }

    @Override
    protected void parseCustom(Element root, DataBindings bindings) {
        List<Element> bindingElements = root.elements("binding");

        for (Element bindingElement : bindingElements) {

            String className = bindingElement.attributeValue("class");
            Preconditions.checkNotNull(className, "Missed 'class' attribute in binding element");

            ExcelConstraints constraints = ClassLoaderUtil.instantiate(className);
            Element configElement = bindingElement.element("config");
            Preconditions.checkNotNull(configElement, "Missed 'config' element in binding element");

            constraints.configure(configElement);

            Element conditionElement = bindingElement.element("condition");
            String condition = conditionElement != null ? conditionElement.attributeValue("query") : null;
            if (bindings.getCondition() == null) {
                bindings.setCondition(condition);
            }

            Element conditionsElement = bindingElement.element("conditions");
            Preconditions.checkNotNull(conditionsElement, "Missed 'conditions' element in binding element");
            QueryType queryType = QueryType.valueOf(conditionsElement.attributeValue("type"));
            if (bindings.getQueryType() == null) {
                bindings.setQueryType(queryType);
            }

            String variableName = bindingElement.attributeValue("variable");
            if (variableName == null && (bindings.getQueryType() == QueryType.INSERT || bindings.getQueryType() == QueryType.UPDATE)) {
                throw new IllegalArgumentException("Missed 'variable' attribute in binding element");
            }

            DataBinding binding = new DataBinding();
            binding.setConstraints(constraints);
            binding.setVariableName(variableName);

            binding.setCondition(condition);
            binding.setQueryType(queryType);

            String queryRole = conditionsElement.attributeValue("role");
            if (queryRole != null) {
                binding.setQueryRole(QueryRole.valueOf(queryRole));
            }

            binding.setLogicalOperator(LogicalOperator
                    .fromString(conditionsElement.attributeValue("logicalOperator")));

            bindings.getBindings().add(binding);
        }
    }
}
