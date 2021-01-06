package ru.runa.af.web.tag;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.DeleteBotAction;
import ru.runa.af.web.html.BotTableBuilder;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "botListTag")
public class BotListTag extends TitledFormTag {

  private static final long serialVersionUID = 1L;
  private Long botStationId;

  public Long getBotStationId() {
    return botStationId;
  }

  @Attribute(required = false, rtexprvalue = true)
  public void setBotStationId(Long botStationId) {
    this.botStationId = botStationId;
  }

  @Override
  protected void fillFormElement(TD tdFormElement) {
    tdFormElement.addElement(new Input(Input.hidden, IdsForm.ID_INPUT_NAME, Long.toString(botStationId)));
    List<Bot> bots = Delegates.getBotService().getBots(getUser(), botStationId);
    tdFormElement.addElement(new BotTableBuilder(pageContext).buildBotTable(bots));
  }

  @Override
  protected String getTitle() {
    return MessagesBot.TITLE_BOT_LIST.message(pageContext);
  }

  @Override
  protected String getSubmitButtonName() {
    return MessagesCommon.BUTTON_REMOVE.message(pageContext);
  }

  @Override
  protected boolean isMultipleSubmit() {
    return true;
  }

  @Override
  protected List<Map<String, String>> getSubmitButtonsData() {
    List<Map<String, String>> data = Lists.newArrayList();
    Map<String, String> buttonData1 = Maps.newHashMap();
    buttonData1.put("name", getSubmitButtonName());
    buttonData1.put("color", "");
    buttonData1.put("type", Input.SUBMIT);
    buttonData1.put("style", "float: right;");
    buttonData1.put("onclick", ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(getConfirmationPopupParameter(), pageContext));
    Map<String, String> buttonData2 = Maps.newHashMap();
    buttonData2.put("name", MessagesCommon.BUTTON_CREATE.message(pageContext));
    buttonData2.put("color", "");
    buttonData2.put("type", Input.BUTTON);
    buttonData2.put("style", "float: left;");
    buttonData2.put("onclick", getCreateAction());
    data.add(buttonData1);
    data.add(buttonData2);
    return data;
  }

  @Override
  public String getAction() {
    return DeleteBotAction.DELETE_BOT_ACTION_PATH;
  }

  public String getCreateAction() {
    return String.format("window.location=\"/wfe/add_bot.do?botStationId=%s\"", botStationId);
  }

  @Override
  public boolean isSubmitButtonEnabled() {
    return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredSingleton.BOTSTATIONS);
  }

  @Override
  public String getConfirmationPopupParameter() {
    return ConfirmationPopupHelper.REMOVE_BOT_PARAMETER;
  }
}