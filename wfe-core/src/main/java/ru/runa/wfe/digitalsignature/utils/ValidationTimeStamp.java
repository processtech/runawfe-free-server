package ru.runa.wfe.digitalsignature.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.Attributes;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.tsp.TimeStampToken;

public class ValidationTimeStamp {
    private TsaClient tsaClient;

    /**
     * @param tsaUrl The url where TS-Request will be done.
     * @throws NoSuchAlgorithmException
     * @throws MalformedURLException
     */
    public ValidationTimeStamp(String tsaUrl) throws NoSuchAlgorithmException, MalformedURLException {
        if (tsaUrl != null) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            this.tsaClient = new TsaClient(new URL(tsaUrl), null, null, digest);
        }
    }

    /**
     * @param content InputStream of the content to sign
     * @return the byte[] of the timestamp token
     * @throws IOException
     */
    public byte[] getTimeStampToken(InputStream content) throws IOException {
        TimeStampToken timeStampToken = tsaClient.getTimeStampToken(IOUtils.toByteArray(content));
        return timeStampToken.getEncoded();
    }

    /**
     * Extend cms signed data with TimeStamp first or to all signers
     *
     * @param signedData Generated CMS signed data
     * @return CMSSignedData Extended CMS signed data
     * @throws IOException
     */
    public CMSSignedData addSignedTimeStamp(CMSSignedData signedData)
            throws IOException {
        SignerInformationStore signerStore = signedData.getSignerInfos();
        List<SignerInformation> newSigners = new ArrayList<SignerInformation>();

        for (SignerInformation signer : signerStore.getSigners()) {
            // This adds a timestamp to every signer (into his unsigned attributes) in the signature.
            newSigners.add(signTimeStamp(signer));
        }

        return CMSSignedData.replaceSigners(signedData, new SignerInformationStore(newSigners));
    }

    /**
     * @param signer information about signer
     * @return information about SignerInformation
     * @throws IOException
     */
    private SignerInformation signTimeStamp(SignerInformation signer)
            throws IOException {
        AttributeTable unsignedAttributes = signer.getUnsignedAttributes();
        ASN1EncodableVector vector = new ASN1EncodableVector();
        if (unsignedAttributes != null) {
            vector = unsignedAttributes.toASN1EncodableVector();
        }
        TimeStampToken timeStampToken = tsaClient.getTimeStampToken(signer.getSignature());
        byte[] token = timeStampToken.getEncoded();
        ASN1ObjectIdentifier oid = PKCSObjectIdentifiers.id_aa_signatureTimeStampToken;
        ASN1Encodable signatureTimeStamp = new Attribute(oid,
                new DERSet(ASN1Primitive.fromByteArray(token)));
        vector.add(signatureTimeStamp);
        Attributes signedAttributes = new Attributes(vector);

        return SignerInformation.replaceUnsignedAttributes(signer, new AttributeTable(signedAttributes));
    }
}
