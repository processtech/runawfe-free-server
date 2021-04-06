package ru.runa.common.web.tag;

import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "link")
public class IdLinkBaseTag extends LinkTag {

    private static final long serialVersionUID = 3956510676485830962L;
    private Long identifiableId;

    @Attribute(required = false, rtexprvalue = true)
    public void setIdentifiableId(Long identifiableId) {
        this.identifiableId = identifiableId;
    }

    public Long getIdentifiableId() {
        return identifiableId;
    }
}
