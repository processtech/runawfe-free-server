package ru.runa.wfe.office.doc;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class LoopOperation extends AbstractIteratorOperation {
    private String iteratorVariableName;
    private Object iterator;
    private XWPFParagraph headerParagraph;
    private final List<XWPFParagraph> bodyParagraphs = Lists.newArrayList();

    public String getIteratorVariableName() {
        return iteratorVariableName;
    }

    public void setIteratorVariableName(String iteratorName) {
        this.iteratorVariableName = iteratorName;
    }

    public List<XWPFParagraph> getBodyParagraphs() {
        return bodyParagraphs;
    }

    public WfVariable getIteratorVariable(Object value) {
        VariableDefinition definition = new VariableDefinition(iteratorVariableName, null);
        definition.setFormat(getIteratorFormatClassName());
        WfVariable variable = new WfVariable(definition, value);
        return variable;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && iteratorVariableName != null;
    }

    public Object getIterator() {
        return iterator;
    }

    public void setIterator(Object iterator) {
        this.iterator = iterator;
    }

    public XWPFParagraph getHeaderParagraph() {
        return headerParagraph;
    }

    public void setHeaderParagraph(XWPFParagraph headerParagraph) {
        this.headerParagraph = headerParagraph;
    }
}
