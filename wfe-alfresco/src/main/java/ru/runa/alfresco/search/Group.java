package ru.runa.alfresco.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups search conditions with selected operator.
 * @author dofs
 */
public class Group extends Expr {
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String NOT = "NOT";
    
    String groupOperator;
    List<Group> children = new ArrayList<Group>();
    
    public Group(N operand, Op operator, Object... params) {
        super(operand, operator, params);
    }
    
    public Group(String groupOperator, N operand, Op operator, Object... params) {
        super(operand, operator, params);
        this.groupOperator = groupOperator;
    }
    
    public Group(String groupOperator, Group... expressions) {
        this.groupOperator = groupOperator;
        for (Group groupExpression : expressions) {
            children.add(groupExpression);
        }
    }

    public List<Group> getChildren() {
        return children;
    }
    
    public void add(Group group) {
        children.add(group);
    }
    
    public boolean isSimple() {
        return children.size() == 0;
    }
    
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("(");
        if (children.size() == 0) {
            if (groupOperator != null) {
                buffer.append(" ").append(groupOperator).append(" ");
            }
            buffer.append(super.toString());
        } else {
            for (int i = 0; i < children.size(); i++) {
                if (i != 0) {
                    buffer.append(" ").append(groupOperator).append(" ");
                }
                buffer.append(children.get(i).toString());
            }
        }
        buffer.append(")");
        return buffer.toString();
    }
}
