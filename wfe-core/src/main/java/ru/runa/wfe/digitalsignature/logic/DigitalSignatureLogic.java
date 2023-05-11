package ru.runa.wfe.digitalsignature.logic;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.digitalsignature.utils.PKCS12Container;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.user.User;
import ru.runa.wfe.digitalsignature.dao.DigitalSignatureDao;

public class DigitalSignatureLogic extends CommonLogic {

    @Autowired
    private DigitalSignatureDao digitalSignatureDao;

    public boolean isDigitalSignatureExist(User user, Long id) {
        if (!digitalSignatureDao.isDigitalSignatureExist(id)) {
            return false;
        }
        DigitalSignature digitalSignature = digitalSignatureDao.getDigitalSignature(id);
        permissionDao.checkAllowed(user, Permission.READ, digitalSignature);
        return true;
    }

    public DigitalSignature create(User user, DigitalSignature digitalSignature) {
        permissionDao.checkAllowed(user, Permission.CREATE_DIGITAL_SIGNATURE, SecuredSingleton.SYSTEM);
        log.info("Creating digital signature");
        digitalSignatureDao.create(digitalSignature);
        permissionDao.setPermissions(user.getActor(), ApplicablePermissions.listVisible(digitalSignature), digitalSignature);
        return digitalSignature;
    }

    public DigitalSignature getDigitalSignature(User user, Long id) {
        DigitalSignature digitalSignature = digitalSignatureDao.getDigitalSignature(id);
        if (digitalSignature == null) {
            return null;
        }
        permissionDao.checkAllowed(user, Permission.READ, digitalSignature);
        try {
            PKCS12Container pc = new PKCS12Container(digitalSignature);
            pc.updateUserDataFromContainer();
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        return digitalSignature;
    }

    public DigitalSignature update(User user, DigitalSignature digitalSignature) {
        permissionDao.checkAllowed(user, Permission.UPDATE, digitalSignature);
        try {
            log.info("Updating digital signature" + digitalSignature.getCommonName());
            PKCS12Container pc = new PKCS12Container(digitalSignature);
            pc.createContainer();
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        return digitalSignatureDao.update(digitalSignature);
    }

    public void remove(User user, DigitalSignature digitalSignature) {
        if (!permissionDao.isAllowed(user, Permission.DELETE, digitalSignature)) {
            throw new AuthorizationException(user.getName() + " digital signature can not be removed");
        }
        log.info("Removing digital signature" + digitalSignature.getCommonName());
        digitalSignatureDao.remove(digitalSignature);
    }
}
