package ru.runa.wfe.office.doc;

import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;

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
    
    public void execute(DocxConfig config, IVariableProvider variableProvider) {
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("placeholder", placeholder).add("value", value).toString();
    }

}
