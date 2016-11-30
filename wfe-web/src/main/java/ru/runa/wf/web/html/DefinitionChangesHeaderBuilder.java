package ru.runa.wf.web.html;

import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.wfe.presentation.*;

import javax.servlet.jsp.PageContext;


public class DefinitionChangesHeaderBuilder implements HeaderBuilder {
   private FieldDescriptor[] displayFields;
   private PageContext pageContext;

    public DefinitionChangesHeaderBuilder(BatchPresentation batchPresentation, PageContext pageContext) {
        this.displayFields = batchPresentation.getDisplayFields();
        this.pageContext = pageContext;
    }

    @Override
    public TR build() {
        TR tr = new TR();
        tr.addElement(new TH(Messages.getMessage(displayFields[0].displayName, pageContext)).setWidth("15%").setClass(Resources.CLASS_LIST_TABLE_TH));
        tr.addElement(new TH(Messages.getMessage(displayFields[1].displayName, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
        tr.addElement(new TH(Messages.getMessage(displayFields[2].displayName, pageContext)).setWidth("13%").setClass(Resources.CLASS_LIST_TABLE_TH));
        tr.addElement(new TH(Messages.getMessage(displayFields[3].displayName, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
        return tr;
    }
}