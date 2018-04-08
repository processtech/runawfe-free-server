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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.Native2AsciiHelper;
import ru.runa.af.web.action.BotTaskConfigurationDownloadAction;
import ru.runa.af.web.action.UpdateBotTaskConfigurationAction;
import ru.runa.af.web.action.UpdateBotTasksAction;
import ru.runa.af.web.form.BotTasksForm;
import ru.runa.af.web.system.TaskHandlerClassesInformation;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "botTaskListTag")
public class BotTaskListTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;
    private Long botId;

    @Attribute(required = false, rtexprvalue = true)
    public void setBotId(Long botId) {
        this.botId = botId;
    }

    public Long getBotId() {
        return botId;
    }

    @Override
    public boolean isFormButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        tdFormElement.addElement(new Input(Input.hidden, IdsForm.ID_INPUT_NAME, Long.toString(botId)));
        BotService botService = Delegates.getBotService();
        getForm().setEncType(Form.ENC_UPLOAD);
        boolean disabled = !Delegates.getAuthorizationService().isAllowed(getUser(), Permission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
        List<BotTask> tasks = botService.getBotTasks(getUser(), botId);
        int nameSize = 1;
        for (BotTask botTask : tasks) {
            if (botTask.getName().length() > nameSize) {
                nameSize = botTask.getName().length();
            }
        }
        RowBuilder rowBuilder = new BotTaskRowBuilder(tasks, disabled, nameSize + 10, pageContext);
        HeaderBuilder headerBuilder = new BotTaskHeaderBuilder(pageContext);
        TableBuilder tableBuilder = new TableBuilder();
        tdFormElement.addElement(tableBuilder.build(headerBuilder, rowBuilder));
    }

    @Override
    protected String getTitle() {
        return MessagesBot.TITLE_BOT_TASK_LIST.message(pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateBotTasksAction.UPDATE_BOT_TASKS_ACTION_PATH;
    }

    static class BotTaskHeaderBuilder implements HeaderBuilder {

        private final PageContext context;

        public BotTaskHeaderBuilder(PageContext pageContext) {
            context = pageContext;
        }

        @Override
        public TR build() {
            TR tr = new TR();
            tr.addElement(new TH(HTMLUtils.createSelectionStatusPropagator()).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesBot.LABEL_BOT_TASK_DETAILS.message(context)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesBot.LABEL_BOT_TASK_SEQUENTIAL.message(context)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

    class BotTaskRowBuilder implements RowBuilder {

        private final Iterator<BotTask> iterator;

        private final boolean disabled;

        private final int nameSize;

        private final PageContext pageContext;

        public BotTaskRowBuilder(List<BotTask> tasks, boolean disabled, int nameSize, PageContext pageContext) {
            this.disabled = disabled;
            iterator = tasks.iterator();
            this.nameSize = nameSize;
            this.pageContext = pageContext;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public TR buildNext() {
            TR tr = new TR();
            BotTask task = iterator.next();
            tr.addElement(buildCheckboxTD(task));
            tr.addElement(buildComplexTD(task));
            tr.addElement(buildSequentialTD(task));
            return tr;
        }

        private TD buildComplexTD(BotTask task) {
            Table table = new Table();
            table.setClass(Resources.CLASS_LIST_TABLE);
            table.setWidth("100%");

            TR nameTr = new TR();
            nameTr.addElement(new TD(MessagesBot.LABEL_BOT_TASK_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            nameTr.addElement(buildNameTD(task));
            table.addElement(nameTr);

            TR handlerTr = new TR();
            handlerTr.addElement(new TD(MessagesBot.LABEL_BOT_TASK_HANDLER.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            handlerTr.addElement(buildHandlerTD(task));
            table.addElement(handlerTr);

            TR configTr = new TR();
            configTr.addElement(new TD(MessagesBot.LABEL_BOT_TASK_CONFIG.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            configTr.addElement(buildConfigurationUploadTD(task));
            table.addElement(configTr);

            TD resTD = new TD();
            resTD.addElement(table);
            return resTD;
        }

        private TD buildNameTD(BotTask task) {
            TD resTD = new TD();
            resTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            Input input = new Input(Input.TEXT, BotTasksForm.BOT_TASK_INPUT_NAME_PREFIX + task.getId() + BotTasksForm.NAME_INPUT_NAME,
                    task.getName());
            input.setDisabled(disabled);
            input.setSize(nameSize);
            resTD.addElement(input);
            return resTD;
        }

        private TD buildHandlerTD(BotTask task) {
            TD resTD = new TD();
            resTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            Select select = new Select();
            select.setName(BotTasksForm.BOT_TASK_INPUT_NAME_PREFIX + task.getId() + BotTasksForm.HANDLER_INPUT_NAME);
            select.setDisabled(disabled);
            String taskHandlerClazz = task.getTaskHandlerClassName();
            boolean isHandlerPresent = false;
            for (String className : TaskHandlerClassesInformation.getClassNames()) {
                boolean isCurrent = className.equalsIgnoreCase(taskHandlerClazz);
                if (isCurrent) {
                    isHandlerPresent = true;
                }
                select.addElement(HTMLUtils.createOption(className, isCurrent));
            }
            if (!isHandlerPresent) {
                String handlerName = MessagesBot.LABEL_UNKNOWN_BOT_HANDLER.message(pageContext) + ": " + taskHandlerClazz;
                select.addElement(HTMLUtils.createOption(handlerName, true));
            }
            resTD.addElement(select);
            return resTD;
        }

        private TD buildCheckboxTD(BotTask task) {
            TD checkboxTD = new TD();
            Input checkBoxInput = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, String.valueOf(task.getId()));
            checkBoxInput.setChecked(true);
            checkBoxInput.setDisabled(disabled);
            checkboxTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            checkboxTD.addElement(checkBoxInput);
            return checkboxTD;
        }

        private TD buildSequentialTD(BotTask task) {
            TD checkboxTD = new TD();
            Input input = new Input(Input.CHECKBOX, BotTasksForm.BOT_TASK_INPUT_NAME_PREFIX + task.getId() + BotTasksForm.SEQUENTIAL_INPUT_NAME);
            input.setDisabled(disabled);
            input.setChecked(task.isSequentialExecution());
            checkboxTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            checkboxTD.addElement(input);
            return checkboxTD;
        }

        @Override
        public List<TR> buildNextArray() {
            return null;
        }
    }

    private TD buildConfigurationUploadTD(BotTask task) {
        TD fileUploadTD = new TD();
        fileUploadTD.setClass(Resources.CLASS_LIST_TABLE_TD);
        Input fileUploadInput = new Input(Input.FILE, BotTasksForm.BOT_TASK_INPUT_NAME_PREFIX + task.getId() + BotTasksForm.CONFIG_FILE_INPUT_NAME);
        fileUploadTD.addElement(fileUploadInput);
        if (task.getConfiguration() != null && task.getConfiguration().length > 0) {
            A link = new A(Commons.getActionUrl(BotTaskConfigurationDownloadAction.DOWNLOAD_BOT_TASK_CONFIGURATION_ACTION_PATH, "id", task.getId(),
                    pageContext, PortletUrlType.Action), MessagesBot.LABEL_BOT_TASK_CONFIG_DOWNLOAD.message(pageContext));
            link.setClass(Resources.CLASS_LINK);
            fileUploadTD.addElement(link);

            StringBuilder str = new StringBuilder();
            str.append("&nbsp;");
            str.append(MessagesBot.LABEL_BOT_TASK_CONFIG_EDIT.message(pageContext));
            boolean configurationIsXml = true;
            try {
                XmlUtils.parseWithoutValidation(task.getConfiguration());
            } catch (Exception e) {
                configurationIsXml = false;
            }
            if (!configurationIsXml && !Native2AsciiHelper.isNeedConvert(new String(task.getConfiguration()))) {
                str.append("*");
            }
            StringBuilder jsLink = new StringBuilder("javascript:");
            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("id", task.getId());
            parameterMap.put("edit", "true");
            jsLink.append("openDocumentEditor('");
            jsLink.append(Commons.getActionUrl(BotTaskConfigurationDownloadAction.DOWNLOAD_BOT_TASK_CONFIGURATION_ACTION_PATH, parameterMap,
                    pageContext, PortletUrlType.Action));
            jsLink.append("','");
            jsLink.append(Commons.getActionUrl(UpdateBotTaskConfigurationAction.UPDATE_TASK_HANDLER_CONF_ACTION_PATH, "id", task.getId(), pageContext,
                    PortletUrlType.Action));
            jsLink.append("','");
            jsLink.append(MessagesCommon.BUTTON_SAVE.message(pageContext));
            jsLink.append("','");
            jsLink.append(MessagesCommon.BUTTON_CANCEL.message(pageContext));
            jsLink.append("');");

            A editLink = new A(jsLink.toString(), str.toString());
            editLink.setClass(Resources.CLASS_LINK);
            fileUploadTD.addElement(editLink);
        }
        return fileUploadTD;
    }
}
