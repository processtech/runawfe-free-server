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
    public void remove(User user, Long id) {
        try {
            getDigitalSignatureService().remove(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public boolean isDigitalSignatureExist(User user, Long id) {
        try {
            return getDigitalSignatureService().isDigitalSignatureExist(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

}
