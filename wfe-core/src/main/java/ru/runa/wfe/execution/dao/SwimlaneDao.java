package ru.runa.wfe.execution.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedSwimlane;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.execution.CurrentSwimlane;

@Component
public class SwimlaneDao extends GenericDao2<Swimlane, CurrentSwimlane, CurrentSwimlaneDao, ArchivedSwimlane, ArchivedSwimlaneDao> {

    @Autowired
    SwimlaneDao(CurrentSwimlaneDao dao1, ArchivedSwimlaneDao dao2) {
        super(dao1, dao2);
    }
}
