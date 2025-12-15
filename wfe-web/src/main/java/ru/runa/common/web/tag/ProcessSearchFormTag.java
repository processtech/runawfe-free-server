package ru.runa.common.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.MessagesBatch;


@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "processSearchForm")
public class ProcessSearchFormTag extends VisibleTag {
    private static final long serialVersionUID = 4330152132857034206L;

    private int size = 30;

    public String getPlaceholder() {
        return MessagesBatch.SEARCH_PLACEHOLDER.message(pageContext);
    }

    public String getButtonText() {
        return MessagesBatch.SEARCH_BUTTON.message(pageContext);
    }

    @Override
    protected ConcreteElement getEndElement() {
        Table table = new Table();
        table.setStyle("margin: 10px 0;");

        TR tr = new TR();
        TD td = new TD();

        Form form = new Form();
        form.setMethod("GET");
        form.setStyle("display: inline;");

        Input input = new Input();
        input.setType(Input.TEXT);
        input.setName("search");
        input.setSize(this.size);
        input.addAttribute("placeholder", getPlaceholder());

        String searchQuery = pageContext.getRequest().getParameter("search");
        if (searchQuery != null) {
            input.setValue(searchQuery);
        }

        Input submit = new Input();
        submit.setType(Input.SUBMIT);
        submit.setValue(getButtonText());

        form.addElement(input);
        form.addElement(new StringElement("&nbsp;"));
        form.addElement(submit);

        td.addElement(form);
        tr.addElement(td);
        table.addElement(tr);

        return table;
    }

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setSize(int size) {
        this.size = size;
    }
}
