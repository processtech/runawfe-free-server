package ru.runa.wf.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.FormPresentationUtils;
import ru.runa.wfe.definition.DefinitionClassPresentation;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "htmlFilter")
public class HtmlFilterTag extends TitledFormTag {

    private static final long serialVersionUID = 3401243801430914L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        byte[] htmlBytes = (byte[]) pageContext.getRequest().getAttribute("htmlBytes");
        Long processDefinitionId = (Long) pageContext.getRequest().getAttribute("processDefinitionId");
        String pageHref = (String) pageContext.getRequest().getAttribute("pageHref");
        String filtered = FormPresentationUtils.adjustUrls(pageContext, processDefinitionId, pageHref, htmlBytes);
        tdFormElement.addElement(filtered);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(DefinitionClassPresentation.DESCRIPTION, pageContext);
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

}
