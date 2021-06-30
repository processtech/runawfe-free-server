package ru.runa.wf.web.ftl.component;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.handler.StorageBindingsParser;
import ru.runa.wfe.office.storage.handler.StoreServiceFactory;
import ru.runa.wfe.office.storage.projection.ProjectionModel;
import ru.runa.wfe.office.storage.projection.Visibility;
import ru.runa.wfe.office.storage.services.StoreHelperImpl;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;

/**
 * @author Alekseev Mikhail
 * @since #1394
 */
@CommonsLog
public class SelectFromInternalStorage extends AbstractUserTypeList implements FormComponentSubmissionHandler {
    @SuppressWarnings("unchecked")
    @Override
    protected UserTypeListModel parseParameters() {
        final List<ProjectionModel> projections = ProjectionModel.parse(getParameterAsString(3));
        final ExecutionResult executionResult = execute(projections);
        final List<String> attributeNames = getVisibleAttributeNames(projections);
        final WfVariable outputVariable = variableProvider.getVariableNotNull(getVariableNameForSubmissionProcessing());

        final List<UserTypeMap> entities = executionResult == ExecutionResult.EMPTY ?
                Collections.emptyList() :
                (List<UserTypeMap>) executionResult.getValue();
        webHelper.getRequest().getSession().setAttribute(getClass().getSimpleName() + outputVariable.getDefinition().getName(), entities);

        return new SelectUserTypeListModel(getDisplayVariable(outputVariable, entities), attributeNames, getParameterAsString(1).equals("many"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ?> extractVariables(
            Interaction interaction,
            VariableDefinition variableDefinition,
            Map<String, ?> userInput, Map<String,
            String> errors
    ) {
        final Map<String, Object> result = new HashMap<>();
        final HttpSession session = webHelper.getRequest().getSession();

        final String attributeName = getClass().getSimpleName() + variableDefinition.getName();
        final List<UserTypeMap> entities = (List<UserTypeMap>) session.getAttribute(attributeName);
        result.put(
                getVariableNameForSubmissionProcessing(),
                variableDefinition.isUserType() ? extractSingleItem(userInput, entities) : extractMultipleItems(userInput, entities)
        );

        session.removeAttribute(attributeName);
        return result;
    }

    @Override
    public String getVariableNameForSubmissionProcessing() {
        return getParameterAsString(4);
    }

    private ExecutionResult execute(Iterable<ProjectionModel> projections) throws InternalApplicationException {
        final String predicates = XmlUtils.unwrapCdata(getParameterAsString(2));

        final DataSource internalStorage = DataSourceStorage.getDataSource(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME);
        final StoreService storeService = StoreServiceFactory.create(internalStorage, variableProvider);

        try {
            final DataBindings bindings = new StorageBindingsParser().parse(predicates);
            final DataBinding binding = Iterables.getOnlyElement(bindings.getBindings());

            final StoreHelper storeHelper = new StoreHelperImpl(bindings, variableProvider, storeService);
            storeHelper.setVariableFormat(variableProvider.getVariableNotNull(getVariableNameForSubmissionProcessing()).getDefinition().getFormatNotNull());
            return storeHelper.execute(binding, variableProvider.getUserType(getParameterAsString(0)), projections);
        } catch (Exception e) {
            log.error("Error executing command on DataStore " + internalStorage.getName(), e);
            throw new InternalApplicationException(e);
        }
    }

    private List<String> getVisibleAttributeNames(List<ProjectionModel> projections) {
        final List<String> attributeNames = new ArrayList<>(projections.size());
        for (ProjectionModel projection : projections) {
            if (projection.getVisibility() == Visibility.VISIBLE) {
                attributeNames.add(projection.getFieldName());
            }
        }
        return attributeNames;
    }

    private Object extractSingleItem(Map<String, ?> userInput, List<UserTypeMap> executionResult) {
        final String[] indexes = (String[]) userInput.get(getVariableNameForSubmissionProcessing());
        if (indexes == null || indexes.length == 0) {
            return null;
        }

        return executionResult.get(TypeConversionUtil.convertTo(int.class, indexes[0]));
    }

    private List<Object> extractMultipleItems(Map<String, ?> userInput, List<UserTypeMap> executionResult) {
        final String[] indexes = (String[]) userInput.get(getVariableNameForSubmissionProcessing());
        if (indexes == null || indexes.length == 0) {
            return Collections.emptyList();
        }

        final List<Object> result = new ArrayList<>(indexes.length);
        for (String index : indexes) {
            result.add(executionResult.get(TypeConversionUtil.convertTo(int.class, index)));
        }
        return result;
    }

    private WfVariable getDisplayVariable(WfVariable variable, List<UserTypeMap> entities) {
        if (!variable.getDefinition().isUserType()) {
            variable.setValue(entities);
            return variable;
        }

        final UserType userType = variableProvider.getUserType(getParameterAsString(0));
        final ListFormat listFormat = new ListFormat();
        listFormat.setComponentUserTypes(new UserType[]{ userType });
        listFormat.setComponentClassNames(new String[]{ userType.getName() });
        final VariableDefinition definition = new VariableDefinition(
                variable.getDefinition().getName(),
                variable.getDefinition().getScriptingName(),
                listFormat
        );
        return new WfVariable(definition, entities);
    }

    public class SelectUserTypeListModel extends UserTypeListModel {
        private final boolean selectList;

        public SelectUserTypeListModel(WfVariable variable, List<String> attributeNames, boolean selectList) {
            super(variable, attributeNames, true);
            this.selectList = selectList;
        }

        @Override
        public List<VariableDefinition> getAttributes() {
            final LinkedList<VariableDefinition> attributes = new LinkedList<>();
            for (String field : attributeNames) {
                VariableDefinition expandedDefinition = userType.getAttributeExpanded(field);
                Preconditions.checkNotNull(expandedDefinition, field);
                attributes.add(expandedDefinition);
            }
            return attributes;
        }

        public boolean isSelectList() {
            return selectList;
        }
    }
}
