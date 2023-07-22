package ru.runa.alfresco.search;

/**
 * Operator for query condition.
 * 
 * @author dofs
 */
public enum Op {
    TYPE_OF("TYPE:\"$operand\""), EQUALS("$operand:\"$0\""), RANGE_INCLUSIVE("$operand:[$0 TO $1]"), LESS_THAN_INCLUSIVE("$operand:[MIN TO $0]"), GREATER_THAN_INCLUSIVE(
            "$operand:[$0 TO MAX]"), RANGE_EXCLUSIVE("$operand:{$0 TO $1}"), LESS_THAN_EXCLUSIVE("$operand:{MIN TO $0}"), GREATER_THAN_EXCLUSIVE(
            "$operand:{$0 TO MAX}"), IS_NULL("ISNULL:\"$operand\""), IS_NOT_NULL("ISNOTNULL:\"$operand\""), PRIMARYPARENT(
            "PRIMARYPARENT:\"$operand\"");

    private String regexp;

    private Op(String regexp) {
        this.regexp = regexp;
    }

    public String getRegexp() {
        return regexp;
    }
}
