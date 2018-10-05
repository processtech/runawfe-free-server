package ru.runa.wfe.office.storage.handler;

import com.google.common.base.Preconditions;
import java.util.List;
import org.dom4j.Element;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.office.excel.ExcelConstraints;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.QueryType;

public class StorageBindingsParser extends FilesSupplierConfigParser<DataBindings> {

    @Override
    protected DataBindings instantiate() {
        return new DataBindings();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void parseCustom(Element root, DataBindings bindings) throws Exception {
        List<Element> bindingElements = root.elements("binding");

        for (Element bindingElement : bindingElements) {

            String className = bindingElement.attributeValue("class");
            Preconditions.checkNotNull(className, "Missed 'class' attribute in binding element");

            String variableName = bindingElement.attributeValue("variable");
            Preconditions.checkNotNull(variableName, "Missed 'variable' attribute in binding element");

            ExcelConstraints constraints = ClassLoaderUtil.instantiate(className);
            Element configElement = bindingElement.element("config");
            Preconditions.checkNotNull(configElement, "Missed 'config' element in binding element");

            constraints.configure(configElement);

            Element conditionElement = bindingElement.element("condition");
            if (bindings.getCondition() == null) {
                bindings.setCondition(conditionElement.attributeValue("query"));
            }

            Element conditionsElement = bindingElement.element("conditions");
            if (bindings.getQueryType() == null) {
                bindings.setQueryType(QueryType.valueOf(conditionsElement.attributeValue("type")));
            }

            DataBinding binding = new DataBinding();
            binding.setConstraints(constraints);
            binding.setVariableName(variableName);

            bindings.getBindings().add(binding);
        }
    }
}
