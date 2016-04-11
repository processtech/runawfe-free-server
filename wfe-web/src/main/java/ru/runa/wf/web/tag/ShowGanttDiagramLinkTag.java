package ru.runa.wf.web.tag;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.IdLinkBaseTag;

/**
 * @jsp.tag name = "showGanttDiagramLink" body-content = "empty"
 */
public class ShowGanttDiagramLinkTag extends IdLinkBaseTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.LABEL_SHOW_GANTT_DIAGRAM, pageContext);
    }
}
