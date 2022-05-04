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
import java.util.ListIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class DemoSubordinateRecursive {

    private final Log log = LogFactory.getLog(DemoSubordinateRecursive.class);

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
            LinkedList<Actor> list = new LinkedList<Actor>();
            LinkedList<Actor> subordinatesList = new LinkedList<Actor>();
            Actor actor = executorDao.getActorByCode(Long.parseLong((String) parameters[0]));
            List<Actor> actors = executorDao.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged());
            DemoChiefFunction demoChiefFunction = new DemoChiefFunction();
            for (Actor currentActor : actors) {
                try {
                    Object[] currentActorCode = new Object[] { currentActor.getCode() };
                    if (demoChiefFunction.getExecutors(currentActorCode).size() > 0) {
                        list.add(currentActor);
                    }
                } catch (OrgFunctionException e) {
                    log.warn("DemoSubordinateRecursive getSubordinateActors. Chief is not proper defined forActor", e);
                }
            }

            findDirectSubordinates(list, subordinatesList, actor, demoChiefFunction);
            findIndirectSubordinates(list, subordinatesList, demoChiefFunction);

            return subordinatesList;
        } catch (Exception e) {
            throw new OrgFunctionException(e);
        }
    }

    /**
     * @param list
     * @param subordinatesList
     * @param actor
     * @param demoChiefFunction
     * @throws OrgFunctionException
     */
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

    /**
     * @param list
     * @param subordinatesList
     * @param demoChiefFunction
     * @throws OrgFunctionException
     */
    private void findIndirectSubordinates(LinkedList<Actor> list, LinkedList<Actor> subordinatesList, DemoChiefFunction demoChiefFunction)
            throws OrgFunctionException {
        int flag = -1;
        while (flag != 0) {
            LinkedList<Actor> newGeneratedSubordinates = new LinkedList<Actor>();
            for (ListIterator<Actor> iter = subordinatesList.listIterator(); iter.hasNext();) {
                findDirectSubordinates(list, newGeneratedSubordinates, iter.next(), demoChiefFunction);
            }
            flag = addNotContainedElements(subordinatesList, newGeneratedSubordinates);
        }
    }

    /**
     * @param subordinatesList
     * @param flag
     * @param newSubordinates
     * @return
     */
    private int addNotContainedElements(LinkedList<Actor> subordinatesList, LinkedList<Actor> newGeneratedSubordinates) {
        int flag = 0;
        for (ListIterator<Actor> iter = newGeneratedSubordinates.listIterator(); iter.hasNext();) {
            Actor acurr = iter.next();
            if (!subordinatesList.contains(acurr)) {
                subordinatesList.add(acurr);
                flag = -1;
            }
        }

        return flag;
    }
}
