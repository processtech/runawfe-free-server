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
package ru.runa.wfe.presentation;

import ru.runa.wfe.commons.PropertyResources;

public class ClassPresentationResources {
    private static final PropertyResources RESOURCES = new PropertyResources("class.presentation.properties", false);

    public static FieldState getFieldState(String property) {
        if (property.startsWith("batch_presentation.")) {
            property = property.substring(19);
        }
        if (property.startsWith(ClassPresentation.editable_prefix + "name:batch_presentation.")) {
            property = property.substring((ClassPresentation.editable_prefix + "name:batch_presentation.").length());
        }
        String value = RESOURCES.getStringProperty(property);
        if (value == null) {
            return FieldState.ENABLED;
        }
        if (value.equalsIgnoreCase("ENABLED")) {
            return FieldState.ENABLED;
        }
        if (value.equalsIgnoreCase("DISABLED")) {
            return FieldState.DISABLED;
        }
        if (value.equalsIgnoreCase("HIDDEN")) {
            return FieldState.HIDDEN;
        }
        throw new IllegalArgumentException("Property " + property + " must be enabled, hidden or disabled; but found " + value);
    }
}
