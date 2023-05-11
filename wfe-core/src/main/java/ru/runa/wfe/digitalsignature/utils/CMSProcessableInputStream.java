package ru.runa.wfe.digitalsignature.utils;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSTypedData;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.pdfbox.io.IOUtils;

class CMSProcessableInputStream implements CMSTypedData {
    private InputStream in;
    private final ASN1ObjectIdentifier contentType;

    CMSProcessableInputStream(InputStream is) {
        this(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()), is);
    }

    CMSProcessableInputStream(ASN1ObjectIdentifier type, InputStream is) {
        contentType = type;
        in = is;
    }

    @Override
    public Object getContent() {
        return in;
    }

    @Override
    public void write(OutputStream out) throws IOException, CMSException {
        IOUtils.copy(in, out);
        in.close();
    }

    @Override
    public ASN1ObjectIdentifier getContentType() {
        return contentType;
    }
}
