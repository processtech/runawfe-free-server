package ru.runa.common.web.html;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Commons;
import ru.runa.common.web.GroupState;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ExpandCollapseGroupAction;
import ru.runa.common.web.form.GroupForm;
import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.common.web.form.SetSortingForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.service.delegate.Delegates;

public class ProcessRowBuilder extends ReflectionRowBuilder {
    private List<WfProcess> allProcesses;

    public ProcessRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, ItemUrlStrategy itemUrlStrategy, TDBuilder[] builders) {
        super(items, batchPresentation, pageContext, actionUrl, returnAction, itemUrlStrategy, builders);
    }

    public ProcessRowBuilder(List<? extends Object> items, BatchPresentation batchPresentation, PageContext pageContext, String actionUrl,
            String returnAction, String idPropertyName, TDBuilder[] builders) {
        super(items, batchPresentation, pageContext, actionUrl, returnAction, builders);
        itemUrlStrategy = new DefaultItemUrlStrategy(idPropertyName, pageContext);
    }

    @Override
    public List<TR> buildNextArray() {
        List<TR> trs = renderTRFromCurrentStateArray();
        int curIdx = currentState.getItemIndex();
        if (currentState.isGroupHeader()) {
            curIdx--;
        }
        do {
            currentState = currentState.buildNextState(batchPresentation);
        } while (currentState.getStateType().equals(GroupState.StateType.TYPE_EMPTY_STATE));

        return trs;
    }

    protected List<TR> renderTRFromCurrentStateArray() {
        FieldDescriptor[] fieldsToDisplayNames = batchPresentation.getAllFields();
        List<TR> result = new ArrayList<TR>();
        Object item = items.get(currentState.getItemIndex());
        List<WfProcess> subrocesses = Delegates.getExecutionService().getSubprocesses(Commons.getUser(pageContext.getSession()),
                ((WfProcess) item).getId(), true);
        if (currentState.isGroupHeader()
                && fieldsToDisplayNames[currentState.getCurrentGrouppedColumnIdx()].displayName.startsWith(ClassPresentation.filterable_prefix)) {
            result.add(buildGroupHeader(subrocesses));
        } else if (currentState.isGroupHeader()) {
            result.add(super.buildGroupHeader());
        } else {
            result.addAll(buildItemRows(subrocesses));
        }

        return result;
    }

    protected List<TR> buildItemRows(List<WfProcess> listSubProcessInstance) {
        List<TR> result = new ArrayList<TR>();

        for (WfProcess subProcessInstance : listSubProcessInstance) {
            result.add(buildItemRow(subProcessInstance));
        }

        return result;
    }

    protected TR buildGroupHeader(List<WfProcess> listSubProcessInstance) {
        Object item = items.get(currentState.getItemIndex());

        TR tr = new TR();
        createEmptyCells(tr, currentState.getGroupIndex() + currentState.getAdditionalColumn());

        TD td = new TD();
        if (listSubProcessInstance.size() > 0) {
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
            tr.addElement(td);
        } else {
            td.addElement(Entities.NBSP);
            tr.addElement(td);
            td.setClass(Resources.CLASS_GROUP_NAME);
        }

        List<Object> listGroupTDBuilders = new ArrayList<Object>();
        for (FieldDescriptor fieldDescriptor : Arrays.asList(batchPresentation.getGrouppedFields())) {
            listGroupTDBuilders.add(fieldDescriptor.getTDBuilder());
        }

        for (int i = 0; i < builders.length; i++) {
            td = builders[i].build(item, env);
            td.setClass(Resources.CLASS_GROUP_NAME);

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

    @Override
    protected List<? extends Object> getItems() {
        if (allProcesses == null) {
            allProcesses = new ArrayList<WfProcess>();
            for (Object item : items) {
                List<WfProcess> listSubProcessInstance = Delegates.getExecutionService().getSubprocesses(Commons.getUser(pageContext.getSession()),
                        ((WfProcess) item).getId(), true);
                allProcesses.add((WfProcess) item);
                allProcesses.addAll(listSubProcessInstance);
            }
        }

        return allProcesses;
    }
}
