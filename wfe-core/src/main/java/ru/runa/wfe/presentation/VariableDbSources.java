package ru.runa.wfe.presentation;

import com.google.common.base.Preconditions;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.var.CurrentVariable;
import ru.runa.wfe.var.impl.CurrentDateVariable;
import ru.runa.wfe.var.impl.CurrentDoubleVariable;
import ru.runa.wfe.var.impl.CurrentLongVariable;
import ru.runa.wfe.var.impl.CurrentStringVariable;

/**
 * Implementation of {@link DbSource} interface for referencing variable values.
 *
 * @author Dofs
 */
public class VariableDbSources {

    /**
     * Creates DB sources for variable search
     * 
     * @param processPath
     *            process path join expression, can be <code>null</code>
     */
    public static DbSource[] get(String processPath) {
        return new DbSource[] {
                new BaseVariableDbSource(CurrentVariable.class, processPath),
                new StorableVariableDbSource(CurrentDateVariable.class),
                new StorableVariableDbSource(CurrentDoubleVariable.class),
                new StorableVariableDbSource(CurrentLongVariable.class),
                new StringVariableDbSource(CurrentStringVariable.class)
        };
    }

    /**
     * Used as inheritance root and for filtering.
     */
    public static class BaseVariableDbSource extends DbSource {
        static final String STRING_VALUE = "stringValue";
        private final String processPath;

        BaseVariableDbSource(Class<?> sourceObject, String processPath) {
            super(sourceObject);
            this.processPath = processPath;
        }

        @Override
        public String getJoinExpression(String alias) {
            StringBuilder join = new StringBuilder(ClassPresentation.classNameSQL);
            if (!Utils.isNullOrEmpty(processPath)) {
                join.append(".").append(processPath);
            }
            join.append("=").append(alias).append(".process");
            return join.toString();
        }

        @Override
        public String getValueDBPath(AccessType accessType, String alias) {
            if (accessType == AccessType.FILTER) {
                return alias == null ? STRING_VALUE : alias + "." + STRING_VALUE;
            }
            // Formerly, class inherited from DefaultDBSource but passed valueDBPath = null to super constructor.
            // And this method (being called from HibernateCompilerInheritanceOrderBuilder.buildOrderToField with alias = null)
            // called super (which would return valueDBPath = null) if accessType != FILTER.
            // Making sure I understood it right:
            Preconditions.checkArgument(alias == null);  // Formerly, junk (alias + ".null") would be returned otherwise.
            return null;
        }
    }

    /**
     * Used only for sorting
     */
    public static class StorableVariableDbSource extends DefaultDbSource {

        StorableVariableDbSource(Class<?> sourceObject) {
            super(sourceObject, "storableValue");
        }
    }

    /**
     * Used only for sorting
     */
    public static class StringVariableDbSource extends DefaultDbSource {

        StringVariableDbSource(Class<?> sourceObject) {
            super(sourceObject, BaseVariableDbSource.STRING_VALUE);
        }
    }
}
