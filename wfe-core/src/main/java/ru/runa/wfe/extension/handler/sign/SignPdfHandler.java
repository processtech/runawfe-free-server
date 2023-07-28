package ru.runa.wfe.extension.handler.sign;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.digitalsignature.utils.PKCS12Container;
import ru.runa.wfe.digitalsignature.dao.DigitalSignatureDao;
import ru.runa.wfe.digitalsignature.utils.CreateVisualSignature;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;

import java.security.KeyStore;

public class SignPdfHandler extends CommonParamBasedHandler {
    @Autowired
    private DigitalSignatureDao digitalSignatureDao;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        FileVariable inputFile = handlerData.getInputParamValueNotNull(FileVariable.class, "inputFile");
        Executor executor =  handlerData.getInputParamValueNotNull(Executor.class, "signer");

        DigitalSignature digitalSignature = digitalSignatureDao.getDigitalSignature(executor.getId());
        if (digitalSignature!=null && !digitalSignature.isDigitalSignatureValid()) {
            log.error("Digital signature of user " + executor.getName() +" is not valid");
            handlerData.setOutputParam("signedFile", inputFile);
            return;
        }
        PKCS12Container pkcs12Container = new PKCS12Container(digitalSignature, null);
        pkcs12Container.updateUserDataFromContainer();
        KeyStore keyStore = pkcs12Container.getKeyStore();

        CreateVisualSignature createVisualSignature = new CreateVisualSignature(keyStore, "nopassword".toCharArray());
        FileVariableImpl fileToSign = new FileVariableImpl(inputFile.getName(), "pdf");
        String tsaurl = SystemProperties.getTSAurl();
        fileToSign.setData(createVisualSignature.signPDF(inputFile.getData(),
                null,tsaurl,executor.getFullName(), "Runa wfe server","workflow"));
        handlerData.setOutputParam("signedFile", fileToSign);
    }
}
