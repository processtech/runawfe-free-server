package ru.runa.wfe.definition.dao;

import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinitionPack;
import ru.runa.wfe.definition.QProcessDefinitionPack;

/**
 * DAO for {@link ProcessDefinitionPack}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class ProcessDefinitionPackDao extends GenericDao<ProcessDefinitionPack> {

    public ProcessDefinitionPackDao() {
        super(ProcessDefinitionPack.class);
    }

    @Override
    protected void checkNotNull(ProcessDefinitionPack entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

    public List<Long> findAllIds() {
        QProcessDefinitionPack p = QProcessDefinitionPack.processDefinitionPack;
        return queryFactory.selectDistinct(p.id).from(p).orderBy(p.id.asc()).fetch();
    }

    /**
     * @throws DefinitionDoesNotExistException If not found.
     */
    public ProcessDefinitionPack getByName(@NonNull String name) {
        QProcessDefinitionPack p = QProcessDefinitionPack.processDefinitionPack;
        val o = queryFactory.selectFrom(p).from(p).where(p.name.eq(name)).fetchFirst();
        if (o == null) {
            throw new DefinitionDoesNotExistException(name);
        }
        return o;
    }

}
