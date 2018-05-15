package ru.runa.wfe.presentation.dao;

import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.QBatchPresentation;

@Component
public class BatchPresentationDAO extends GenericDAO<BatchPresentation> {

    public List<BatchPresentation> getAllShared() {
        QBatchPresentation bp = QBatchPresentation.batchPresentation;
        return queryFactory.selectFrom(bp).where(bp.shared.isTrue()).fetch();
    }
}
