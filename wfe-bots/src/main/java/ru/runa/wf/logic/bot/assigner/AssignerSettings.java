package ru.runa.wf.logic.bot.assigner;

import java.util.List;

import com.google.common.collect.Lists;

public class AssignerSettings {
    private final List<Condition> conditions = Lists.newArrayList();

    public void addAssignerCondition(Condition condition) {
        conditions.add(condition);
    }

    public List<Condition> getAssignerConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        return conditions.toString();
    }

    public static class Condition {
        private final String swimlaneName;
        private final String functionClassName;
        private final String variableName;

        public Condition(String swimlaneName, String functionClassName, String variableName) {
            this.swimlaneName = swimlaneName;
            this.functionClassName = functionClassName;
            this.variableName = variableName;
        }

        public String getFunctionClassName() {
            return functionClassName;
        }

        public String getSwimlaneName() {
            return swimlaneName;
        }

        public String getVariableName() {
            return variableName;
        }

        @Override
        public String toString() {
            return swimlaneName + " | " + functionClassName + " | " + variableName;
        }
    }
}
