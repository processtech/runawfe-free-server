package ru.runa.wfe.presentation.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.QBatchPresentation;

@Component
public class BatchPresentationDao extends GenericDao<BatchPresentation> {

    public BatchPresentationDao() {
        super(BatchPresentation.class);
    }

    public List<BatchPresentation> getAllShared() {
        val bp = QBatchPresentation.batchPresentation;
        return queryFactory.selectFrom(bp).where(bp.shared.isTrue()).fetch();
    }
}
