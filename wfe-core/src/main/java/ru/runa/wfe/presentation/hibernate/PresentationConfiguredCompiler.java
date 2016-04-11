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

import java.util.List;

import org.hibernate.Query;

import ru.runa.wfe.presentation.BatchPresentation;

/**
 * Creates {@link Query} to load data according to {@link BatchPresentation}.
 */
public class PresentationConfiguredCompiler<T extends Object> extends PresentationCompiler<T> implements
        IBatchPresentationConfiguredCompiler<T> {

    /**
     * Parameters, used to create last hibernate query or set explicitly to compiler.
     */
    private final CompilerParameters configuredParameters;

    /**
     * Creates component to build loading data {@link Query}.
     * 
     * @param batchPresentation
     *            {@link BatchPresentation}, used to load data.
     */
    public PresentationConfiguredCompiler(BatchPresentation batchPresentation, CompilerParameters parameters) {
        super(batchPresentation);
        this.configuredParameters = parameters;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getBatch() {
        return getBatchQuery(new CompilerParameters(configuredParameters, false)).list();
    }

    @Override
    public int getCount() {
        return ((Number) getBatchQuery(new CompilerParameters(configuredParameters, true)).uniqueResult()).intValue();
    }
}
