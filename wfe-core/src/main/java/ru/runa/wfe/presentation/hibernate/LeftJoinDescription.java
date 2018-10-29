package ru.runa.wfe.presentation.hibernate;

/**
 * Describes left join, applied to SQL query.
 */
class LeftJoinDescription {

    /**
     * Left join expression. 
     */
    public String leftJoinExpression;

    /**
     * Table name, used as root for left join (BEFORE statement left join; to which we joins).
     */
    public String rootTableName;

    /**
     * Creates left join description.
     * @param leftJoin Left join expression. 
     * @param rootTableName Table name, used as root for left join (BEFORE statement left join; to which we joins).
     */
    public LeftJoinDescription(String leftJoin, String rootTableName) {
        leftJoinExpression = leftJoin;
        this.rootTableName = rootTableName;
    }
}
