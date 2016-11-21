/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.presentation.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;

/**
 * Holds mapping from {@link FieldDescriptor} to alias, assigned to field in HQL
 * query and vice verse.
 */
public class HibernateCompilerAliasMapping {

    /**
     * Map {@link FieldDescriptor} to HQL query alias.
     */
    private final Map<FieldDescriptor, String> fieldToAlias = new HashMap<FieldDescriptor, String>();

    /**
     * Map HQL query alias to {@link FieldDescriptor}.
     */
    private final Map<String, FieldDescriptor> aliasToField = new HashMap<String, FieldDescriptor>();

    /**
     * Creates mapping from {@link FieldDescriptor} to alias for specified
     * {@link BatchPresentation}.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation} to initialize alias mappings.
     */
    public HibernateCompilerAliasMapping(BatchPresentation batchPresentation) {
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        for (int idx = 0; idx < fields.length; ++idx) {
            FieldDescriptor field = fields[idx];
            if (field.dbSources == null) {
                throw new InternalApplicationException("Field dbSource is null. Something wrong with " + batchPresentation);
            }
            if (field.dbSources[0].getSourceObject().equals(batchPresentation.getClassPresentation().getPresentationClass())) {
                addAliasMapping(field, ClassPresentation.classNameSQL);
            } else if (field.displayName.startsWith(ClassPresentation.removable_prefix)) {
                addAliasMapping(field, "editedField" + idx);
            } else if (field.displayName.startsWith(ClassPresentation.editable_prefix)) {
                ;
            } else {
                // Field value getter from class, unrelated with main class
                addAliasMapping(field, "field" + idx);
            }
        }
    }

    /**
     * Return's HQL query alias for given field.
     * 
     * @param field
     *            Field, to get HQL alias.
     * @return HQL query alias.
     */
    public String getAlias(FieldDescriptor field) {
        return fieldToAlias.get(field);
    }

    /**
     * Returns field, corresponds to HQL alias.
     * 
     * @param alias
     *            HQL query alias.
     * @return Field for HQL alias.
     */
    public FieldDescriptor getField(String alias) {
        return aliasToField.get(alias);
    }

    /**
     * Returns all HQL aliases, created for {@link BatchPresentation}.
     * 
     * @return All HQL aliases.
     */
    public Set<String> getAliases() {
        return aliasToField.keySet();
    }

    /**
     * Returns all fields in {@link BatchPresentation}.
     * 
     * @return All {@link BatchPresentation} fields.
     */
    public Set<FieldDescriptor> getFields() {
        return fieldToAlias.keySet();
    }

    /**
     * Add field and alias to corresponding map's
     * 
     * @param field
     *            Field, to add.
     * @param alias
     *            Alias for field.
     */
    private void addAliasMapping(FieldDescriptor field, String alias) {
        fieldToAlias.put(field, alias);
        aliasToField.put(alias, field);
    }
}
