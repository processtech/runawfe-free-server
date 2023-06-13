package ru.runa.wfe.extension.orgfunction;

import com.google.common.base.Objects;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.val;
import ru.runa.wfe.extension.OrgFunctionException;
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
            val subordinatesList = new LinkedList<Actor>();
            Actor actor = executorDao.getActorByCode(Long.parseLong((String) parameters[0]));
            DemoChiefFunction demoChiefFunction = new DemoChiefFunction();
            findDirectSubordinates(subordinatesList, actor, demoChiefFunction);
            findIndirectSubordinates(subordinatesList, demoChiefFunction);
            return subordinatesList;
        } catch (Exception e) {
            throw new OrgFunctionException(e);
        }
    }

    private int findDirectSubordinates(LinkedList<Actor> subordinatesList, Actor actor, DemoChiefFunction demoChiefFunction)
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

    private void findIndirectSubordinates(LinkedList<Actor> subordinatesList, DemoChiefFunction demoChiefFunction)
            throws OrgFunctionException {
        int flag = -1;
        while (flag != 0) {
            LinkedList<Actor> newGeneratedSubordinates = new LinkedList<>();
            for (Actor actor : subordinatesList) {
                findDirectSubordinates(newGeneratedSubordinates, actor, demoChiefFunction);
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
