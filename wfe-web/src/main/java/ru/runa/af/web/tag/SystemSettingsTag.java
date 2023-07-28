package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "systemSettings")
public class SystemSettingsTag extends TitledFormTag {
    private static final long serialVersionUID = -426375016105456L;

    @Override
    protected String getTitle() {
        return MessagesCommon.MAIN_MENU_ITEM_SETTINGS.message(pageContext);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        if (!Delegates.getExecutorService().isAdministrator(getUser())) {
            throw new AuthorizationException("No permission on this page");
        }
        Table table = new Table();
        for (String resource : EditSettingsTag.settingsList.descendingKeySet()) {
            TR tr = new TR();
            String title = EditSettingsTag.getDescription(pageContext, resource);
            tr.addElement("<td><a href=edit_settings.do?resource=" + resource + ">" + title + "</a></td>");
            table.addElement(tr);
        }
        tdFormElement.addElement(table);
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    /*
     * @Override protected String getSubmitButtonName() { return Messages.getMessage(Messages.BUTTON_USE_DAFAULT_PROPERTIES, pageContext); }
     * 
     * @Override public String getAction() { return RestoreDefaultSettingsAction.RESTORE_DEFAULT_SETTINGS_ACTION_PATH; }
     * 
     * @Override public String getConfirmationPopupParameter() { return ConfirmationPopupHelper.USE_DEFAULT_PROPERTIES_PARAMETER; }
     */
}
