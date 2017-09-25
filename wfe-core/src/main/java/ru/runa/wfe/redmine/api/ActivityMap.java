package ru.runa.wfe.redmine.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Отображает активити из внутренниго типа ActivityType на
 * множество идентификаторов активити которые используются в redmine.
 * redmine использует следующие идентификаторы активити:
 * DESIGN = 8
 * DEVELOP = 9
 * PM = 10
 * MEETING = 12
 * REQUIREMENTS = 13
 * @author Veniamin
 *
 */
public class ActivityMap {

    private static final Map<ActivityType, Integer> map = 
            new HashMap<ActivityType, Integer>();
    
    static {
        map.put(ActivityType.DESIGN, 8);
        map.put(ActivityType.DEVELOP, 9);
        map.put(ActivityType.PM, 10);
        map.put(ActivityType.MEETING, 12);
        map.put(ActivityType.REQUIREMENTS, 13);
    }
    
    /**
     * Возвращает число которое является идентификатором активити в redmine.
     * @param activityType - тип активити.
     * @return
     */
    public static Integer getType(ActivityType activityType) {
        Objects.requireNonNull(activityType);
    
        return map.get(activityType);
    }
}
