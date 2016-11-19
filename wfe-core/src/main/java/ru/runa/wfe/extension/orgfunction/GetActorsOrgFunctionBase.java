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
package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.extension.OrgFunction;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.user.Executor;

import com.google.common.base.Throwables;

/**
 * Created on 08.01.2007
 **/
public abstract class GetActorsOrgFunctionBase extends OrgFunction {

    @Override
    public final List<? extends Executor> getExecutors(Object... parameters) throws OrgFunctionException {
        try {
            List<Long> codes = getActorCodes(parameters);
            log.debug("Actor codes result: " + codes);
            return executorDAO.getActorsByCodes(codes);
        } catch (Exception e) {
            Throwables.propagateIfPossible(e, OrgFunctionException.class);
            throw new OrgFunctionException(e);
        }
    }

    protected abstract List<Long> getActorCodes(Object... parameters);

}
