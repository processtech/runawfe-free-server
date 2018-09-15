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
package ru.runa.wfe.definition;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that definition name differs from the name of existing definition (during redeploy).
 */
public class DefinitionNameMismatchException extends InternalApplicationException {
    private static final long serialVersionUID = -2137340395617831247L;
    private final String expectedProcessDefinitionName;
    private final String givenProcessDefinitionName;

    public DefinitionNameMismatchException(String expectedProcessDefinitionName, String givenProcessDefinitionName) {
        super("Expected definition name " + expectedProcessDefinitionName);
        this.expectedProcessDefinitionName = expectedProcessDefinitionName;
        this.givenProcessDefinitionName = givenProcessDefinitionName;
    }

    public String getExpectedProcessDefinitionName() {
        return expectedProcessDefinitionName;
    }

    public String getGivenProcessDefinitionName() {
        return givenProcessDefinitionName;
    }
}
