package ru.runa.wfe.presentation.filter;

public class AnywhereStringFilterCriteria extends StringFilterCriteria {
    private static final long serialVersionUID = -1849845246809052465L;

    public AnywhereStringFilterCriteria() {
        super();
    }

    public AnywhereStringFilterCriteria(String filterValue) {
        super(filterValue);
    }

    @Override
    public String getFilterTemplate(int position) {
        return ANY_SYMBOLS + super.getFilterTemplate(position) + ANY_SYMBOLS;
    }
}
