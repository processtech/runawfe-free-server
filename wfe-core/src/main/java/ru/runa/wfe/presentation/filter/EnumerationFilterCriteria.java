package ru.runa.wfe.presentation.filter;

import java.util.Map;

import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

/**
 * Base class for filter criteria's, supporting selecting from predefined set of
 * values.
 */
public abstract class EnumerationFilterCriteria extends FilterCriteria {

    private static final long serialVersionUID = 1L;

    /**
     * {@link Map} from enumerated value to property display name (struts
     * property).
     */
    private final Map<String, String> enumerationValues;

    /**
     * Creates instance, with specified allowed values.
     * 
     * @param enumerationValues
     *            {@link Map} from enumerated value to property display name
     *            (struts property).
     */
    protected EnumerationFilterCriteria(Map<String, String> enumerationValues) {
        super(1);
        this.enumerationValues = enumerationValues;
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        StringBuilder sb = new StringBuilder(aliasedFieldName);
        sb.append(" = '").append(getFilterTemplate(0)).append("' ");
        return sb.toString();
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        super.validate(newTemplates);
        if (!enumerationValues.keySet().contains(newTemplates[0])) {
            throw new IllegalArgumentException("Value " + newTemplates[0] + " is not allowed by enumeration criteria of type "
                    + this.getClass().getName());
        }
    }
}
