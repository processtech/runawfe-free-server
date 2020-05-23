package ru.runa.af.web.tag;

import java.util.List;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.RemoveRelationAction;
import ru.runa.af.web.form.RelationForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.CheckboxTdBuilder;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listRelationsForm")
public class ListRelationsFormTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.READ, SecuredSingleton.RELATIONS);
        List<Relation> relations = Delegates.getRelationService().getRelations(getUser(), getBatchPresentation());
        TableBuilder tableBuilder = new TableBuilder();
        TdBuilder checkboxBuilder = new CheckboxTdBuilder(null, null) {

            @Override
            protected String getIdValue(Object object) {
                return String.valueOf(((Relation) object).getId());
            }

            @Override
            protected boolean isEnabled(Object object, Env env) {
                return true;
            }
        };
        TdBuilder[] builders = BatchPresentationUtils.getBuilders(new TdBuilder[] { checkboxBuilder }, getBatchPresentation(), null);
        RowBuilder rowBuilder = new ReflectionRowBuilder(relations, getBatchPresentation(), pageContext, WebResources.ACTION_MAPPING_MANAGE_RELATION,
                getReturnAction(), new RelationURLStrategy(), builders);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(getBatchPresentation(), 1, 0, getReturnAction(), pageContext);
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_RELATIONS.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    public String getAction() {
        return RemoveRelationAction.ACTION_PATH;
    }

    class RelationURLStrategy implements ItemUrlStrategy {

        @Override
        public String getUrl(String baseUrl, Object item) {
            return Commons.getActionUrl(baseUrl, RelationForm.RELATION_ID, ((Relation) item).getId(), pageContext, PortletUrlType.Action);
        }

    }
}
