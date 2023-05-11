package ru.runa.wfe.extension.handler.sign;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.digitalsignature.utils.PKCS12Container;
import ru.runa.wfe.digitalsignature.dao.DigitalSignatureDao;
import ru.runa.wfe.digitalsignature.utils.CheckSignature;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.file.FileVariable;


public class CheckSignPdfHandler extends CommonParamBasedHandler {

    @Autowired
    private DigitalSignatureDao digitalSignatureDao;

    boolean signIsValid;
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        FileVariable inputFile = handlerData.getInputParamValueNotNull(FileVariable.class, "file");
        Executor executor =  handlerData.getInputParamValueNotNull(Executor.class, "signer");
        CheckSignature showSignature = new CheckSignature();
        DigitalSignature digitalSignature = digitalSignatureDao.getDigitalSignature(executor.getId());
        if (digitalSignature!=null && !digitalSignature.isDigitalSignatureValid()) {
            log.error("Digital signature of user " + executor.getName() +" is not valid");
            handlerData.setOutputParam("isSigned", false);
            return;
        }
        PKCS12Container container = new PKCS12Container(digitalSignature);
        container.updateUserDataFromContainer();
        try {
            showSignature.execute(inputFile.getData(), container.getX509Certificate());
            signIsValid = true;
        } catch (Exception e) {
            signIsValid = false;
            log.error(e);
        }
        handlerData.setOutputParam("isSigned", signIsValid);
    }
}
