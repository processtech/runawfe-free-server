package ru.runa.wfe.office.doc;

import com.google.common.base.Objects;
import ru.runa.wfe.var.VariableProvider;

public class ReplaceOperation extends Operation {
    protected boolean placeholderRead;
    protected String placeholder;
    protected String value;

    @Override
    public boolean isValid() {
        return placeholderRead;
    }
    
    public void appendPlaceholder(String text) {
        if (placeholder == null) {
            placeholder = text;
        } else {
            placeholder += text;
        }
    }
    
    public String getPlaceholder() {
        return placeholder;
    }
    
    public boolean isStarted() {
        return placeholder != null;
    }

    public boolean isPlaceholderRead() {
        return placeholderRead;
    }
    
    public void setEnded(boolean ended) {
        this.placeholderRead = ended;
        this.placeholder = this.placeholder.trim();
    }
    
    public String getValue() {
        if (value == null) {
            return placeholder;
        }
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public void execute(DocxConfig config, VariableProvider variableProvider) {
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("placeholder", placeholder).add("value", value).toString();
    }

}
