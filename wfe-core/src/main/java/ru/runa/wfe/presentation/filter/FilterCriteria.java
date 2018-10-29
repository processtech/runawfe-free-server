package ru.runa.wfe.presentation.filter;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.Serializable;
import java.util.Arrays;
import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

public abstract class FilterCriteria implements Serializable {
    private static final long serialVersionUID = 1L;
    private String[] filterTemplates;
    private int templatesCount;
    private boolean exclusive;

    /**
     * For web services only
     */
    protected FilterCriteria() {
    }

    protected FilterCriteria(String[] filterTemplates) {
        Preconditions.checkNotNull(filterTemplates);
        this.filterTemplates = filterTemplates;
        templatesCount = filterTemplates.length;
    }

    protected FilterCriteria(int templatesCount) {
        this.templatesCount = templatesCount;
        filterTemplates = new String[templatesCount];
        for (int i = 0; i < filterTemplates.length; i++) {
            filterTemplates[i] = "";
        }
    }

    public int getTemplatesCount() {
        return templatesCount;
    }

    public String[] getFilterTemplates() {
        return filterTemplates;
    }

    public String getFilterTemplate(int position) {
        return filterTemplates[position];
    }

    protected void validate(String[] newTemplates) throws FilterFormatException {
        if (newTemplates.length != templatesCount) {
            throw new IllegalArgumentException("Incorrect parameters count");
        }
    }

    public void applyFilterTemplates(String[] filterTemplates) throws FilterFormatException {
        validate(filterTemplates);
        this.filterTemplates = filterTemplates;
    }

    public abstract String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders);

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        if (exclusive != ((FilterCriteria) obj).isExclusive()) {
            return false;
        }
        if (Arrays.equals(((FilterCriteria) obj).filterTemplates, filterTemplates)) {
            return true;
        }
        return false;
    }

    public String makePlaceHolderName(final String aliasedFieldName) {
        return aliasedFieldName.replaceAll("[\\.|\\,|\\(|\\)|\\s|\\-|\\+|\\*\\:|']", "");
    }

    @Override
    public int hashCode() {
        return Objects.hashCode((Object[]) filterTemplates);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("filters", filterTemplates).toString();
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    protected String buildInOperator(String aliasedFieldName) {
        String where = "";
        String[] values = getFilterTemplate(0).split(",");
        if (values.length > 0) {
            for (String value : values) {
                if (Strings.isNullOrEmpty(where)) {
                    where = aliasedFieldName + " IN (";
                } else {
                    where += ",";
                }
                where += "'" + value.trim() + "'";
            }
            where += ")";
        }
        return where;
    }

    protected String buildBetweenOperator(String aliasedFieldName) {
        String where = "";
        String[] values = getFilterTemplate(0).split("-");
        if (values.length == 2) {
            where = aliasedFieldName + " BETWEEN '" + values[0].trim() + "' AND '" + values[1].trim() + "'";
        }
        return where;
    }

}
