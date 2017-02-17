/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.common.web.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.GroupState;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ExpandCollapseGroupAction;
import ru.runa.common.web.form.GroupForm;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.common.web.form.SetSortingForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.ProcessHierarchyUtils;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Gritsenko_S
 */
public class ReflectionRowBuilder implements RowBuilder {

    class EnvImpl extends EnvBaseImpl {

        @Override
        public PageContext getPageContext() {
            return pageContext;
        }

        @Override
        public BatchPresentation getBatchPresentation() {
            return batchPresentation;
        }

        @Override
        public String getURL(Object object) {
            return itemUrlStrategy.getUrl(basePartOfUrlToObject, object);
        }

        @Override
        public String getConfirmationMessage(Long pid) {
            if (basePartOfUrlToObject.equals(ru.runa.common.WebResources.ACTION_MAPPING_START_PROCESS)
                    && ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_PARAMETER)
                    || ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER)) {
                Interaction interaction = Delegates.getDefinitionService().getStartInteraction(getUser(), pid);
                if (!(interaction.hasForm() || interaction.getOutputTransitionNames().size() > 1)) {
                    String actionParameter = ConfirmationPopupHelper.START_PROCESS_FORM_PARAMETER;
                    return ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(actionParameter, getPageContext());
                }
            }
            return null;
        }

        @Override
        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) {
            boolean[] retVal = allowedCache.get(permission);
            if (retVal == null) {
                if (extractor == null) {
                    retVal = Delegates.getAuthorizationService().isAllowed(getUser(), permission, (List<Identifiable>) getItems());
                } else {
                    List<Identifiable> identifiables = Lists.newArrayListWithExpectedSize(getItems().size());
                    for (Object object : getItems()) {
                        identifiables.add(extractor.getIdentifiable(object, this));
                    }
                    retVal = Delegates.getAuthorizationService().isAllowed(getUser(), permission, identifiables);
                }
                allowedCache.put(permission, retVal);
            }
            return retVal[currentState.getItemIndex()];
        }

        public boolean isFilterable() {
            boolean isFilterable = false;
            int idx = 0;
            FieldDescriptor[] fields = batchPresentation.getAllFields();
            for (FieldDescriptor field : fields) {
                if (field.displayName.startsWith(ClassPresentation.filterable_prefix) && batchPresentation.isFieldGroupped(idx)) {
                    isFilterable = true;
                    break;
                }
                idx++;
            }

            return isFilterable;
        }

        private final Map<Permission, boolean[]> allowedCache = Maps.newHashMap();

    }

    /**
     * <<<<<<< HEAD TODO This is temporary workaround. Fix should be made in authorization subsystem by refactoring executor permissions. Only 1
     * SecuredObjectType should be introduced for identifiables hierarchy due to simplifying SQL quieries and non-crossing IDs.
     *
     * ======= TODO This is temporary workaround. Fix should be made in authorization subsystem by refactoring executor permissions. Only 1
     * SecuredObjectType should be introduced for identifiables hierarchy due to simplifying SQL quieries and non-crossing IDs.
     * 
     * >>>>>>> cccc94e358152f69386e66af26e0958ae242dcdb
     * 
     * @author dofs
     * @since 4.0.6
     */
    class ExecutorTableEnvImpl extends EnvImpl {
        private final Map<Executor, Boolean> allowedCache = Maps.newHashMap();

        @Override
        public boolean isAllowed(Permission permission, IdentifiableExtractor extractor) {
            List<Executor> executors = (List<Executor>) getItems();
            Executor executor = executors.get(currentState.getItemIndex());
            if (!allowedCache.containsKey(executor)) {
                List<Executor> acquiredExecutors = Lists.newArrayList();
                for (Executor testExecutor : executors) {
                    if (executor.getSecuredObjectType() == testExecutor.getSecuredObjectType()) {
                        acquiredExecutors.add(testExecutor);
                    }
                }
                boolean[] allowedArray = Delegates.getAuthorizationService().isAllowed(getUser(), permission, acquiredExecutors);
                for (int i = 0; i < allowedArray.length; i++) {
                    allowedCache.put(acquiredExecutors.get(i), allowedArray[i]);
                }
            }
            return allowedCache.get(executor);
        }
    }

    protected final List<? extends Object> items;

    private final String basePartOfUrlToObject;

    protected final BatchPresentation batchPresentation;

    protected GroupState currentState;

    protected final String returnAction;

    protected final PageContext pageContext;

    protected ItemUrlStrategy itemUrlStrategy;

    private CssClassStrategy cssClassStrategy;

    protected final int additionalEmptyCells;

    protected final TDBuilder[] builders;
    protected final EnvImpl env;

    public ReflectionRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, String idPropertyName, TDBuilder[] builders) {
        this(items, batchPresentation, pageContext, actionUrl, returnAction, builders);
        itemUrlStrategy = new DefaultItemUrlStrategy(idPropertyName, pageContext);
    }

    public ReflectionRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, ItemUrlStrategy itemUrlStrategy, TDBuilder[] builders) {
        this(items, batchPresentation, pageContext, actionUrl, returnAction, builders);
        this.itemUrlStrategy = itemUrlStrategy;
    }

    protected ReflectionRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, TDBuilder[] builders) {
        this.items = items;
        this.batchPresentation = batchPresentation;
        this.pageContext = pageContext;
        basePartOfUrlToObject = actionUrl;
        this.returnAction = returnAction;
        if (items.size() > 0 && items.get(0) instanceof Executor) {
            env = new ExecutorTableEnvImpl();
        } else {
            env = new EnvImpl();
        }
        currentState = GroupState.createStartState(items, batchPresentation, builders, env);
        additionalEmptyCells = GroupState.getMaxAdditionalCellsNum(batchPresentation, items, env);
        this.builders = builders;
    }

    protected TR buildGroupHeader() {
        TR tr = new TR();
        createEmptyCells(tr, currentState.getGroupIndex() + currentState.getAdditionalColumn());

        IMG groupingImage = null;
        if (currentState.isVisible()) {
            groupingImage = new IMG(Commons.getUrl(Resources.GROUP_MINUS_IMAGE, pageContext, PortletUrlType.Resource));
            groupingImage.setAlt(Resources.GROUP_MINUS_ALT);
        } else {
            groupingImage = new IMG(Commons.getUrl(Resources.GROUP_PLUS_IMAGE, pageContext, PortletUrlType.Resource));
            groupingImage.setAlt(Resources.GROUP_PLUS_ALT);
        }
        groupingImage.setBorder(0);

        String anchorId = currentState.getCurrentGrouppedColumnValue(currentState.getGroupIndex());
        if (anchorId == null) {
            anchorId = "";
        }
        String groupId = currentState.getGroupId();

        TD td = new TD();
        td.setClass(Resources.CLASS_GROUP_NAME);
        td.addElement(new A().setName(anchorId));

        Map<String, String> params = new HashMap<String, String>();
        params.put(SetSortingForm.BATCH_PRESENTATION_ID, batchPresentation.getCategory());
        params.put(GroupForm.GROUP_ID, groupId);
        params.put(ReturnActionForm.RETURN_ACTION, returnAction);
        params.put(GroupForm.GROUP_ACTION_ID, currentState.isVisible() ? GroupForm.GROUP_ACTION_COLLAPSE : GroupForm.GROUP_ACTION_EXPAND);
        String actionUrl = Commons.getActionUrl(ExpandCollapseGroupAction.ACTION_PATH, params, anchorId, pageContext, PortletUrlType.Action);
        A link = new A(actionUrl, groupingImage);

        td.addElement(link);
        link.addElement(Entities.NBSP);
        final String displayName = batchPresentation.getAllFields()[currentState.getCurrentGrouppedColumnIdx()].displayName;
        if (displayName.startsWith(ClassPresentation.removable_prefix)) {
            int end = displayName.lastIndexOf(':');
            int begin = displayName.lastIndexOf(':', end - 1) + 1;
            link.addElement(Messages.getMessage(displayName.substring(begin, end), pageContext) + " '"
                    + displayName.substring(displayName.lastIndexOf(':') + 1) + "':");
        } else if (displayName.startsWith(ClassPresentation.filterable_prefix)) {
            link.addElement(Messages.getMessage(displayName.substring(displayName.lastIndexOf(':') + 1), pageContext));
        } else {
            link.addElement(ru.runa.common.web.Messages.getMessage(displayName, pageContext) + ":");
        }
        link.addElement(Entities.NBSP);
        link.addElement(currentState.getCurrentGrouppedColumnValue());
        td.setColSpan(builders.length + batchPresentation.getGrouppedFields().length - currentState.getGroupIndex() + additionalEmptyCells);
        tr.addElement(td);
        return tr;
    }

    protected TR buildItemRow() {
        Object item = items.get(currentState.getItemIndex());
        return buildItemRow(item);
    }

    protected List<? extends Object> getItems() {
        return items;
    }

    protected TR buildItemRow(Object item) {

        TR tr = new TR();
        if (cssClassStrategy != null) {
            String cssClassName = cssClassStrategy.getClassName(item, env.getUser());
            if (cssClassName != null) {
                tr.setClass(cssClassName);
            }

            String cssStyle = cssClassStrategy.getCssStyle(item);
            if (cssStyle != null) {
                tr.setStyle(cssStyle);
            }
        }
        if (batchPresentation.getGrouppedFields().length > 0) {
            createEmptyCells(tr, currentState.getGroupIndex() + additionalEmptyCells);
        }

        List<Object> listGroupTDBuilders = new ArrayList<Object>();
        for (FieldDescriptor fieldDescriptor : Arrays.asList(batchPresentation.getGrouppedFields())) {
            listGroupTDBuilders.add(fieldDescriptor.getTDBuilder());
        }

        for (int i = 0; i < builders.length; i++) {
            TD td = builders[i].build(item, env);

            if (env.isFilterable()) {
                StringBuilder str = new StringBuilder();
                for (int j = 1; j < ProcessHierarchyUtils.getProcessIdsArray(((WfProcess) item).getHierarchyIds()).length; j++) {
                    str.append(Entities.NBSP);
                }

                String href = null;
                if (td.elements().hasMoreElements()) {
                    ConcreteElement concreteElement = (ConcreteElement) td.elements().nextElement();
                    if (concreteElement instanceof A) {
                        A a = (A) concreteElement;
                        href = a.getAttribute("href");
                        str.append(a.elements().nextElement().toString());
                    }
                }

                if (href != null) {
                    String classAttr = td.getAttribute("class");
                    td = new TD();
                    td.addElement(new A(href, str.toString()));
                    td.setClass(classAttr);
                }
            }

            if (listGroupTDBuilders.contains(builders[i])) {
                if (td.elements().hasMoreElements()) {
                    ConcreteElement concreteElement = (ConcreteElement) td.elements().nextElement();
                    if (concreteElement instanceof A) {
                        A a = (A) concreteElement;
                        if (a.elements().hasMoreElements() && a.elements().nextElement().toString().trim().length() == 0) {
                            String href = a.getAttribute("href");
                            FieldDescriptor fieldDescriptorForBuilder = null;
                            for (FieldDescriptor fieldDescriptor : Arrays.asList(batchPresentation.getGrouppedFields())) {
                                if (builders[i].equals(fieldDescriptor.getTDBuilder())) {
                                    fieldDescriptorForBuilder = fieldDescriptor;
                                }
                            }
                            String message;
                            String displayName = fieldDescriptorForBuilder.displayName;
                            if (displayName.startsWith(ClassPresentation.removable_prefix)) {
                                message = displayName.substring(displayName.lastIndexOf(':') + 1);
                            } else {
                                message = Messages.getMessage(displayName, pageContext);
                            }
                            message += " " + MessagesOther.LABEL_IS_MISSED.message(pageContext);
                            td = new TD();
                            td.addElement(new A(href, message));
                            td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
                        }
                    }
                }
            }
            tr.addElement(td);
        }

        return tr;
    }

    public void setCssClassStrategy(CssClassStrategy cssClassStrategy) {
        this.cssClassStrategy = cssClassStrategy;
    }

    private TR renderTRFromCurrentState() {
        if (currentState.isGroupHeader()) {
            return buildGroupHeader();
        }
        return buildItemRow();
    }

    @Override
    public TR buildNext() {
        TR tr = renderTRFromCurrentState();
        // If element not displayed (in group), we must emulate displaying.
        // int curIdx = currentState.getItemIndex();
        // if (currentState.isGroupHeader()) {
        // curIdx--;
        // }
        do {
            currentState = currentState.buildNextState(batchPresentation);
        } while (currentState.getStateType().equals(GroupState.StateType.TYPE_EMPTY_STATE));
        return tr;
    }

    protected void createEmptyCells(TR tr, int numberOfCells) {
        for (int i = 0; i < numberOfCells; i++) {
            TD cell = new TD();
            cell.addElement(Entities.NBSP);
            tr.addElement(cell);
            cell.setClass(Resources.CLASS_EMPTY20_TABLE_TD);
        }
    }

    @Override
    public boolean hasNext() {
        return !currentState.equals(GroupState.STATE_NO_MORE_ELEMENTS);
    }

    protected static final class DefaultItemUrlStrategy implements ItemUrlStrategy {

        private final PageContext context;
        private final String idPropertyName;

        public DefaultItemUrlStrategy(String idPropertyName, PageContext pageContext) {
            this.idPropertyName = idPropertyName;
            this.context = pageContext;
        }

        @Override
        public String getUrl(String baseUrl, Object item) {
            try {
                String idValue = BeanUtils.getProperty(item, idPropertyName);
                return Commons.getActionUrl(baseUrl, IdForm.ID_INPUT_NAME, idValue, context, PortletUrlType.Action);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }
}
