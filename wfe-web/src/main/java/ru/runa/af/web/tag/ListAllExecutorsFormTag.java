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
package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.RemoveExecutorsAction;
import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.CssClassStrategy;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SecuredObjectCheckboxTdBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.lang.Delegation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.*;

/**
 * Created on 18.08.2004
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listAllExecutorsForm")
public class ListAllExecutorsFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = -7478022960008761625L;

    private boolean buttonEnabled;
    
        private final Set<Class<? extends TemporaryGroup>> notDisplayableExecutorsByDefault;

    public ListAllExecutorsFormTag() {
        this.notDisplayableExecutorsByDefault = new HashSet<>();
        this.notDisplayableExecutorsByDefault.add(TemporaryGroup.class);
        this.notDisplayableExecutorsByDefault.add(DelegationGroup.class);
        this.notDisplayableExecutorsByDefault.add(EscalationGroup.class);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        int executorsCount;
        List<Executor> executors = (List<Executor>) Delegates.getExecutorService().getExecutors(getUser(), getBatchPresentation());
        boolean displayBasicGroups = WebResources
                .getResources()
                .getBooleanProperty("display.only.basic.userGroups", true);
        if (displayBasicGroups) {
            executors.removeIf(e -> notDisplayableExecutorsByDefault.contains(e));
        }
        executorsCount = executors.size();
        BatchPresentation batchPresentation = getBatchPresentation();
        buttonEnabled = BatchPresentationUtils.isExecutorPermissionAllowedForAnyone(getUser(), executors, batchPresentation, Permission.UPDATE);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, executorsCount, getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder[] prefixBuilders = new TdBuilder[] { new SecuredObjectCheckboxTdBuilder(Permission.UPDATE) };
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(prefixBuilders, batchPresentation, null);
        ReflectionRowBuilder rowBuilder = new ReflectionRowBuilder(executors, batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, getReturnAction(), IdForm.ID_INPUT_NAME, builders);
        rowBuilder.setCssClassStrategy(new CssClassStrategy() {

            @Override
            public String getCssStyle(Object item) {
                return null;
            }

            @Override
            public String getClassName(Object item, User user) {
                if (item instanceof Group) {
                    return Resources.CLASS_EXECUTOR_GROUP;
                }
                return null;
            }
        });
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return buttonEnabled;
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_EXECUTORS.message(pageContext);
    }

    @Override
    public String getAction() {
        return RemoveExecutorsAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_EXECUTORS_PARAMETER;
    }
}
