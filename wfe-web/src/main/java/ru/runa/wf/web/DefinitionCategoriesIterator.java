package ru.runa.wf.web;

import java.util.Iterator;
import java.util.List;

import ru.runa.common.web.CategoriesIterator;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class DefinitionCategoriesIterator implements Iterator<String[]> {

    private final CategoriesIterator innerIterator;

    public DefinitionCategoriesIterator(User user) {
        innerIterator = new CategoriesIterator(getAllCategories(user));
    }

    private static List<String[]> getAllCategories(User user) {
        DefinitionService definitionService = Delegates.getDefinitionService();
        BatchPresentation batchPresentation = BatchPresentationFactory.DEFINITIONS.createNonPaged();
        List<WfDefinition> definitions = definitionService.getProcessDefinitions(user, batchPresentation, false);
        return Lists.transform(definitions, new Function<WfDefinition, String[]>() {

            @Override
            public String[] apply(WfDefinition input) {
                return input.getCategories();
            }
        });
    }

    @Override
    public boolean hasNext() {
        return innerIterator.hasNext();
    }

    @Override
    public String[] next() {
        return innerIterator.next();
    }

    @Override
    public void remove() {
        innerIterator.remove();
    }
}
