package ru.runa.wfe.digitalsignature.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.digitalsignature.QDigitalSignature;

@Component
public class DigitalSignatureDao extends CommonDao {
    public static final long ROOT_DIGITAL_SIGNATURE_ACTOR_ID = Long.MAX_VALUE;

    /**
     * Check if digital signature with given name exists.
     *
     * @param actorId
     * *            id to check.
     * @return Returns true, if digital signature with given actorId exists; false otherwise.
     */
    public boolean doesDigitalSignatureExist(Long actorId ) {
        return getDigitalSignature(actorId) != null;
    }

    public boolean doesRootDigitalSignatureExist() {
        return getRootDigitalSignature() != null;
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

    public DigitalSignature createRoot(DigitalSignature digitalSignature) {
        digitalSignature.setActorId(ROOT_DIGITAL_SIGNATURE_ACTOR_ID);
        sessionFactory.getCurrentSession().save(digitalSignature);
        return digitalSignature;
    }

    /**
     * Load {@linkplain DigitalSignature} by actor id. Throws exception if load is impossible.
     *
     * @param actorId
     *
     * @return digital signature with specified actorId.
     */
    public DigitalSignature getDigitalSignature(Long actorId) {
        QDigitalSignature digitalSignature = QDigitalSignature.digitalSignature;
        return queryFactory.selectFrom(digitalSignature).where(digitalSignature.actorId.eq(actorId)).fetchFirst();
    }

    public DigitalSignature getRootDigitalSignature() {
        QDigitalSignature digitalSignature = QDigitalSignature.digitalSignature;
        return queryFactory.selectFrom(digitalSignature).where(digitalSignature.actorId.eq(ROOT_DIGITAL_SIGNATURE_ACTOR_ID)).fetchFirst();
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
