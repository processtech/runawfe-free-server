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

import com.google.common.base.Objects;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorAlreadyExistsException;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * <p>
 * Created on 20.03.2006 18:00:44
 * </p>
 */
@CommonsLog
public class DemoSubordinateRecursive {

    /**
     * @param parameters
     *            array of executor ids. Array size must be 1.
     * @throws ExecutorAlreadyExistsException
     */
    public List<Actor> getSubordinateActors(ExecutorDao executorDao, Object[] parameters) throws OrgFunctionException {
        if (parameters.length != 1) {
            throw new OrgFunctionException("Wrong parameters array: " + Arrays.asList(parameters) + ", expected 1 parameter.");
        }
        try {
            LinkedList<Actor> list = new LinkedList<>();
            LinkedList<Actor> subordinatesList = new LinkedList<>();
            Actor actor = executorDao.getActorByCode(Long.parseLong((String) parameters[0]));
            List<Executor> executors = executorDao.getAllExecutors(BatchPresentationFactory.EXECUTORS.createNonPaged());
            DemoChiefFunction demoChiefFunction = new DemoChiefFunction();
            for (Executor executor : executors) {
                if (executor instanceof Actor) {
                    try {
                        Actor currentActor = (Actor) executor;
                        Object[] currentActorCode = new Object[] { currentActor.getCode() };
                        if (demoChiefFunction.getExecutors(currentActorCode).size() > 0) {
                            list.add(currentActor);
                        }
                    } catch (OrgFunctionException e) {
                        log.warn("DemoSubordinateRecursive getSubordinateActors. Chief is not proper defined forActor", e);
                    }
                }
            }

            findDirectSubordinates(list, subordinatesList, actor, demoChiefFunction);
            findIndirectSubordinates(list, subordinatesList, demoChiefFunction);

            return subordinatesList;
        } catch (Exception e) {
            throw new OrgFunctionException(e);
        }
    }

    private int findDirectSubordinates(LinkedList<Actor> list, LinkedList<Actor> subordinatesList, Actor actor, DemoChiefFunction demoChiefFunction)
            throws OrgFunctionException {
        int result = 0;
        for (Actor acurr : subordinatesList) {
            Object[] currentActorCode = new Object[] { acurr.getCode() };
            Executor chief = demoChiefFunction.getExecutors(currentActorCode).get(0);
            if (Objects.equal(chief, actor) && !Objects.equal(acurr, chief)) {
                subordinatesList.add(acurr);
                result++;
            }
        }
        return result;
    }

    private void findIndirectSubordinates(LinkedList<Actor> list, LinkedList<Actor> subordinatesList, DemoChiefFunction demoChiefFunction)
            throws OrgFunctionException {
        int flag = -1;
        while (flag != 0) {
            LinkedList<Actor> newGeneratedSubordinates = new LinkedList<>();
            for (Actor actor : subordinatesList) {
                findDirectSubordinates(list, newGeneratedSubordinates, actor, demoChiefFunction);
            }
            flag = addNotContainedElements(subordinatesList, newGeneratedSubordinates);
        }
    }

    private int addNotContainedElements(LinkedList<Actor> subordinatesList, LinkedList<Actor> newGeneratedSubordinates) {
        int flag = 0;
        for (Actor actor : newGeneratedSubordinates) {
            if (!subordinatesList.contains(actor)) {
                subordinatesList.add(actor);
                flag = -1;
            }
        }
        return flag;
    }
}
