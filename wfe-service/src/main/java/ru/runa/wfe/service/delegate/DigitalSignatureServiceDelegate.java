package ru.runa.wfe.service.delegate;

import ru.runa.wfe.service.DigitalSignatureService;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.user.User;

public class DigitalSignatureServiceDelegate extends Ejb3Delegate implements DigitalSignatureService {

    public DigitalSignatureServiceDelegate() {
        super(DigitalSignatureService.class);
    }

    private DigitalSignatureService getDigitalSignatureService() {
        return (DigitalSignatureService) getService();
    }

    @Override
    public DigitalSignature create(User user, DigitalSignature digitalSignature) {
        try {
            return getDigitalSignatureService().create(user, digitalSignature);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public DigitalSignature getDigitalSignature(User user, Long id) {
        try {
            return getDigitalSignatureService().getDigitalSignature(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }


    @Override
    public void update(User user, DigitalSignature digitalSignature) {
        try {
            getDigitalSignatureService().update(user, digitalSignature);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
    @Override
    public void updateRoot(User user, DigitalSignature digitalSignature) {
        try {
            getDigitalSignatureService().updateRoot(user, digitalSignature);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void remove(User user, Long id) {
        try {
            getDigitalSignatureService().remove(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean doesDigitalSignatureExist(User user, Long id) {
        try {
            return getDigitalSignatureService().doesDigitalSignatureExist(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean doesRootDigitalSignatureExist(User user) {
        try {
            return getDigitalSignatureService().doesRootDigitalSignatureExist(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] getRootCertificate(User user) {
        return getDigitalSignatureService().getRootCertificate(user);
    }

    @Override
    public DigitalSignature createRoot(User loggedUser, DigitalSignature digitalSignature) {
        return getDigitalSignatureService().createRoot(loggedUser, digitalSignature);
    }

    @Override
    public DigitalSignature getRootDigitalSignature(User user) {
        return getDigitalSignatureService().getRootDigitalSignature(user);
    }

    @Override
    public void removeRootDigitalSignature(User user) {
        try {
            getDigitalSignatureService().removeRootDigitalSignature(user);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
