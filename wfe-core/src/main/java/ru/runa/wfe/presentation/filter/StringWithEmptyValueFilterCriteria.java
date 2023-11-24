package ru.runa.wfe.presentation.filter;

public class StringWithEmptyValueFilterCriteria extends StringFilterCriteria {
    private static final long serialVersionUID = -1849845246809052465L;

    public StringWithEmptyValueFilterCriteria() {
        super();
    }

    public StringWithEmptyValueFilterCriteria(String filterValue) {
        super(filterValue);
    }

    @Override
    public String getFilterTemplate(int position) {
        if (super.getFilterTemplate(position).isEmpty()) {
            return "";
        }
        return ANY_SYMBOLS + super.getFilterTemplate(position) + ANY_SYMBOLS;
    }
}
