package ru.runa.wfe.digitalsignature.utils.cert;

import java.util.Date;

public class RevokedCertificateException extends Exception {
    private static final long serialVersionUID = 3543946618794126654L;

    public RevokedCertificateException(String message, Date revocationTime) {
        super(message);
    }
}
