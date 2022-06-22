package ru.runa.wfe.security.auth;

import java.io.InputStream;
import java.security.Key;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.SneakyThrows;
import ru.runa.wfe.commons.ClassLoaderUtil;

import static ru.runa.wfe.security.auth.SubjectPrincipalsHelper.encryptionType;

class SecuredKeyProvider {
    @SneakyThrows
    Key getKey() {
        final byte[] buffer = new byte[8];
        try (InputStream stream = ClassLoaderUtil.getAsStreamNotNull("runawfe_key", SecuredKeyProvider.class)) {
            for (int length; (length = stream.read(buffer)) != -1; ) ;
        }

        final DESKeySpec desKeySpec = new DESKeySpec(buffer);
        return new SecretKeySpec(desKeySpec.getKey(), encryptionType);
    }
}
