package ru.runa.wfe.digitalsignature.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.digitalsignature.QDigitalSignature;

@Component
public class DigitalSignatureDao extends CommonDao {


    /**
     * Check if digital signature with given name exists.
     *
     * @param actorId
     * *            id to check.
     * @return Returns true, if digital signature with given id exists; false otherwise.
     */
    public boolean isDigitalSignatureExist(Long actorId ) {
        return getDigitalSignature(actorId) != null;
    }

    /**
     * Create digital signature (save it to database). Generate code property for {@linkplain DigitalSignature} with code == 0.
     *
     * @param digitalSignature - Creating digital signature class.
     *
     * @return Returns created digital signature.
     */
    public DigitalSignature create(DigitalSignature digitalSignature) {
        sessionFactory.getCurrentSession().save(digitalSignature);
        return digitalSignature;
    }

    /**
     * Load {@linkplain DigitalSignature} by actor id. Throws exception if load is impossible.
     *
     * @param actorId
     *
     * @return digital signature with specified id.
     */
    public DigitalSignature getDigitalSignature(Long actorId) {
        QDigitalSignature digitalSignature = QDigitalSignature.digitalSignature;
        return queryFactory.selectFrom(digitalSignature).where(digitalSignature.actorId.eq(actorId)).fetchFirst();
    }

    /**
     * Update digital signature.
     *
     * @param digitalSignature  Updated digital signature new state.
     * @return Returns updated digital signature state after update.
     */
    public DigitalSignature update(DigitalSignature digitalSignature) {
        QDigitalSignature qDigitalSignature = QDigitalSignature.digitalSignature;
        queryFactory.update(qDigitalSignature).where(qDigitalSignature.id.eq(digitalSignature.getId()))
                .set(qDigitalSignature.container,digitalSignature.getContainer()).execute();
        return getDigitalSignature(digitalSignature.getActorId());
    }


    public void remove(DigitalSignature digitalSignature) {
        QDigitalSignature qDigitalSignature = QDigitalSignature.digitalSignature;
        queryFactory.delete(qDigitalSignature).where(qDigitalSignature.id.eq(digitalSignature.getId())).execute();
    }

    public void remove(Long actorId) {
        QDigitalSignature qDigitalSignature = QDigitalSignature.digitalSignature;
        queryFactory.delete(qDigitalSignature).where(qDigitalSignature.actorId.eq(actorId)).execute();
    }
}
