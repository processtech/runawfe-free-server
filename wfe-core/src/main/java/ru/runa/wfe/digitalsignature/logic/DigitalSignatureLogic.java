package ru.runa.wfe.digitalsignature.logic;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.digitalsignature.utils.PKCS12Container;
import ru.runa.wfe.digitalsignature.utils.RootPKCS12Container;
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

    public boolean doesDigitalSignatureExist(User user, Long id) {
        if (!digitalSignatureDao.doesDigitalSignatureExist(id)) {
            return false;
        }
        DigitalSignature digitalSignature = digitalSignatureDao.getDigitalSignature(id);
        permissionDao.checkAllowed(user, Permission.READ, digitalSignature);
        return true;
    }
    public boolean doesRootDigitalSignatureExist(User user) {
        if (!digitalSignatureDao.doesRootDigitalSignatureExist()) {
            return false;
        }
        DigitalSignature digitalSignature = digitalSignatureDao.getRootDigitalSignature();
        permissionDao.checkAllowed(user, Permission.READ, digitalSignature);
        return true;
    }
    public DigitalSignature create(User user, DigitalSignature digitalSignature) {
        permissionDao.checkAllowed(user, Permission.CREATE_DIGITAL_SIGNATURE, SecuredSingleton.SYSTEM);
        log.info("Creating digital signature");
        if (!doesRootDigitalSignatureExist(user)) {
            log.error("No  root digital signature was found");
            return null;
        }
        try {
            PKCS12Container dScontainer = new PKCS12Container(digitalSignature,getRootDigitalSignature(user));
            dScontainer.createContainer();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        digitalSignature = digitalSignatureDao.create(digitalSignature);
        permissionDao.setPermissions(user.getActor(), ApplicablePermissions.listVisible(digitalSignature), digitalSignature);
        return digitalSignature;
    }

    public DigitalSignature createRoot(DigitalSignature digitalSignature) {
        log.info("Creating root digital signature");
        try {
            RootPKCS12Container rootDScontainer = new RootPKCS12Container(digitalSignature);
            rootDScontainer.createContainer();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return digitalSignatureDao.createRoot(digitalSignature);
    }

    public DigitalSignature getDigitalSignature(User user, Long actorId) {
        DigitalSignature digitalSignature = digitalSignatureDao.getDigitalSignature(actorId);
        if (digitalSignature == null) {
            return null;
        }
        permissionDao.checkAllowed(user, Permission.READ, digitalSignature);
        try {
            PKCS12Container dSContainer = new PKCS12Container(digitalSignature,getRootDigitalSignature(user));
            dSContainer.updateUserDataFromContainer();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return digitalSignature;
    }
    public DigitalSignature getRootDigitalSignature (User user) {
        DigitalSignature rootDigitalSignature = digitalSignatureDao.getRootDigitalSignature();
        if (rootDigitalSignature == null) {
            return null;
        }
        permissionDao.checkAllowed(user, Permission.READ, rootDigitalSignature);
        try {
            RootPKCS12Container rootDSContainer = new RootPKCS12Container(rootDigitalSignature);
            rootDSContainer.updateUserDataFromContainer();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return rootDigitalSignature;
    }


    public DigitalSignature update(User user, DigitalSignature digitalSignature) {
        permissionDao.checkAllowed(user, Permission.UPDATE, digitalSignature);
        try {
            log.info("Updating digital signature " + digitalSignature.getCommonName());
            DigitalSignature rootDigitalSignature = getRootDigitalSignature(user);
            if (rootDigitalSignature == null) {
                log.error("No  root digital signature was found");
                return null;
            }
            PKCS12Container dSContainer = new PKCS12Container(digitalSignature,getRootDigitalSignature(user));
            dSContainer.updateContainer();
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        return digitalSignatureDao.update(digitalSignature);
    }
    public DigitalSignature updateRoot(User user, DigitalSignature rootDigitalSignature) {
        permissionDao.checkAllowed(user, Permission.UPDATE, rootDigitalSignature);
        try {
            log.info("Updating root digital signature " + rootDigitalSignature.getCommonName());
            RootPKCS12Container dSContainer = new RootPKCS12Container(rootDigitalSignature);
            dSContainer.updateContainer();
        } catch (Exception e) {
            log.warn(e.toString(), e);
        }
        return digitalSignatureDao.update(rootDigitalSignature);
    }


    public void remove(User user, DigitalSignature digitalSignature) {
        if (!permissionDao.isAllowed(user, Permission.DELETE, digitalSignature)) {
            throw new AuthorizationException(user.getName() + " digital signature can not be removed");
        }
        log.info("Removing digital signature" + digitalSignature.getCommonName());
        digitalSignatureDao.remove(digitalSignature);
    }

    public byte[] getRootCertificate(User user) {
        try {
            RootPKCS12Container dSContainer = new RootPKCS12Container(getRootDigitalSignature(user));
            dSContainer.updateUserDataFromContainer();
            return dSContainer.getCertificate().getEncoded();
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }



}
