package ru.runa.wfe.digitalsignature.utils;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.util.Charsets;
import org.apache.pdfbox.util.Hex;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;
import ru.runa.wfe.digitalsignature.utils.cert.CertificateVerificationException;
import ru.runa.wfe.digitalsignature.utils.cert.CertificateVerifier;

public final class CheckSignature {
    protected final Log log = LogFactory.getLog(getClass());

    public CheckSignature() throws IOException {
        Security.addProvider(SecurityProvider.getProvider());
    }

    public void execute(byte[] inputFile, X509Certificate x509Certificate) throws IOException, TSPException, GeneralSecurityException, CertificateVerificationException, OperatorCreationException, CMSException {
        String password = "";
        PDDocument document = null;
        try {
            PDFParser parser = new PDFParser(new RandomAccessBuffer(inputFile), password);
            parser.setLenient(true);
            parser.parse();
            document = parser.getPDDocument();
            if (document.getSignatureDictionaries().isEmpty()) {
                throw new IOException("Signature not found");
            }
            boolean isSignerFound = false;
            for (PDSignature sig : document.getSignatureDictionaries()) {
                byte[] contents = sig.getContents();
                byte[] signedContent = sig.getSignedContent(inputFile);
                String subFilter = sig.getSubFilter();
                if (subFilter != null) {
                    if (subFilter.equals("adbe.pkcs7.detached") || subFilter.equals("ETSI.CAdES.detached")) {
                        if (verifyPKCS7ReturnsCertMatches(signedContent, contents, sig, x509Certificate)) {
                            isSignerFound = true;
                        }
                    }
                }
                int[] byteRange = sig.getByteRange();
                if (byteRange.length != 4) {
                    log.error("Signature byteRange must have 4 items");
                    throw new IOException("Signature byteRange must have 4 items");
                } else {
                    checkContentValueWithFile(new RandomAccessBuffer(inputFile), byteRange, contents);
                }
            }
            analyseDSS(document);
            if (!isSignerFound) {
                throw new IOException("User signature is not found");
            }
        } catch (CMSException ex) {
            throw new IOException(ex);
        } catch (OperatorCreationException ex) {
            throw new IOException(ex);
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }


    private void checkContentValueWithFile(RandomAccessBuffer raf, int[] byteRange, byte[] contents) throws IOException {
        raf.seek(byteRange[1]);
        int c = raf.read();
        if (c != '<') {
            log.error("'<' expected at offset " + byteRange[1] + ", but got " + (char) c);
        }
        byte[] contentFromFile = raf.readFully(byteRange[2] - byteRange[1] - 2);
        byte[] contentAsHex = Hex.getString(contents).getBytes(Charsets.US_ASCII);
        if (contentFromFile.length != contentAsHex.length) {
            log.error("Raw content length from file is " + contentFromFile.length + ", but internal content string in hex has length " + contentAsHex.length);
        }
        for (int i = 0; i < contentFromFile.length; ++i) {
            try {
                if (Integer.parseInt(String.valueOf((char) contentFromFile[i]), 16) != Integer.parseInt(String.valueOf((char) contentAsHex[i]), 16)) {
                    log.error("Possible manipulation at file offset " + (byteRange[1] + i + 1) + " in signature content");
                    break;
                }
            } catch (NumberFormatException ex) {
                log.error("Incorrect hex value");
                log.error("Possible manipulation at file offset " + (byteRange[1] + i + 1) + " in signature content");
                break;
            }
        }
        c = raf.read();
        if (c != '>') {
            log.error("'>' expected at offset " + byteRange[2] + ", but got " + (char) c);
        }
        raf.close();
    }

    /**
     * Verify a PKCS7 signature.
     *
     * @param byteArray the byte sequence that has been signed
     * @param contents  the /Contents field as a COSString
     * @param sig       the PDF signature (the /V dictionary)
     * @throws CMSException
     * @throws OperatorCreationException
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws TSPException
     */

    private boolean verifyPKCS7ReturnsCertMatches(byte[] byteArray, byte[] contents, PDSignature sig, X509Certificate x509Certificate) throws CMSException, OperatorCreationException, IOException, GeneralSecurityException, TSPException, CertificateVerificationException {
        boolean isCertMatches = false;
        CMSProcessable signedContent = new CMSProcessableByteArray(byteArray);
        CMSSignedData signedData = new CMSSignedData(signedContent, contents);
        @SuppressWarnings("unchecked") Store<X509CertificateHolder> certificatesStore = signedData.getCertificates();
        if (certificatesStore.getMatches(null).isEmpty()) {
            throw new IOException("No certificates in signature");
        }
        Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
        if (signers.isEmpty()) {
            throw new IOException("No signers in signature");
        }
        SignerInformation signerInformation = signers.iterator().next();
        @SuppressWarnings("unchecked") Collection<X509CertificateHolder> matches = certificatesStore.getMatches(signerInformation.getSID());
        if (matches.isEmpty()) {
            throw new IOException("Signer '" + signerInformation.getSID().getIssuer() + ", serial# " + signerInformation.getSID().getSerialNumber() + " does not match any certificates");
        }
        X509CertificateHolder certificateHolder = matches.iterator().next();
        X509Certificate certFromSignedData = new JcaX509CertificateConverter().getCertificate(certificateHolder);
        isCertMatches = x509Certificate.equals(certFromSignedData);
        log.info("certFromSignedData: " + certFromSignedData);
        SigUtils.checkCertificateUsage(certFromSignedData);
        TimeStampToken timeStampToken = SigUtils.extractTimeStampTokenFromSignerInformation(signerInformation);
        if (timeStampToken != null) {
            SigUtils.validateTimestampToken(timeStampToken);
            @SuppressWarnings("unchecked") Collection<X509CertificateHolder> tstMatches = timeStampToken.getCertificates().getMatches(timeStampToken.getSID());
            X509CertificateHolder tstCertHolder = tstMatches.iterator().next();
            X509Certificate certFromTimeStamp = new JcaX509CertificateConverter().getCertificate(tstCertHolder);
            // merge both stores using a set to remove duplicates
            HashSet<X509CertificateHolder> certificateHolderSet = new HashSet<X509CertificateHolder>();
            certificateHolderSet.addAll(certificatesStore.getMatches(null));
            certificateHolderSet.addAll(timeStampToken.getCertificates().getMatches(null));
            SigUtils.verifyCertificateChain(new CollectionStore<X509CertificateHolder>(certificateHolderSet), certFromTimeStamp, timeStampToken.getTimeStampInfo().getGenTime());
            SigUtils.checkTimeStampCertificateUsage(certFromTimeStamp);
            // compare the hash of the signature with the hash in the timestamp
            byte[] tsMessageImprintDigest = timeStampToken.getTimeStampInfo().getMessageImprintDigest();
            String hashAlgorithm = timeStampToken.getTimeStampInfo().getMessageImprintAlgOID().getId();
            byte[] sigMessageImprintDigest = MessageDigest.getInstance(hashAlgorithm).digest(signerInformation.getSignature());
            if (Arrays.equals(tsMessageImprintDigest, sigMessageImprintDigest)) {
                log.info("timestamp signature verified");
            } else {
                log.error("timestamp signature verification failed");
            }
        }
        try {
            if (sig.getSignDate() != null) {
                certFromSignedData.checkValidity(sig.getSignDate().getTime());
                log.info("Certificate valid at signing time");
            } else {
                log.error("Certificate cannot be verified without signing time");
            }
        } catch (CertificateExpiredException ex) {
            log.error("Certificate expired at signing time");
        } catch (CertificateNotYetValidException ex) {
            log.error("Certificate not yet valid at signing time");
        }
        if (signerInformation.getSignedAttributes() != null) {
            Attribute signingTime = signerInformation.getSignedAttributes().get(CMSAttributes.signingTime);
            if (signingTime != null) {
                Time timeInstance = Time.getInstance(signingTime.getAttrValues().getObjectAt(0));
                try {
                    certFromSignedData.checkValidity(timeInstance.getDate());
                    log.info("Certificate valid at signing time: " + timeInstance.getDate());
                } catch (CertificateExpiredException ex) {
                    log.error("Certificate expired at signing time");
                } catch (CertificateNotYetValidException ex) {
                    log.error("Certificate not yet valid at signing time");
                }
            }
        }
        if (signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(SecurityProvider.getProvider()).build(certFromSignedData))) {
            log.info("Signature verified");
        } else {
            log.info("Signature verification failed");
        }
        if (CertificateVerifier.isSelfSigned(certFromSignedData)) {
            log.error("Certificate is self-signed.");
        } else {
            log.info("Certificate is not self-signed");
            if (sig.getSignDate() != null) {
                SigUtils.verifyCertificateChain(certificatesStore, certFromSignedData, sig.getSignDate().getTime());
            } else {
                log.error("Certificate cannot be verified without signing time");
            }
        }
        return isCertMatches;
    }

    /**
     * Analyzes the DSS-Dictionary (Document Security Store) of the document. Which is used for
     * signature validation. The DSS is defined in PAdES Part 4 - Long Term Validation.
     *
     * @param document PDDocument, to get the DSS from
     */
    private void analyseDSS(PDDocument document) throws IOException {
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        COSBase dssElement = catalog.getCOSObject().getDictionaryObject("DSS");
        if (dssElement instanceof COSDictionary) {
            COSDictionary dss = (COSDictionary) dssElement;
            log.info("DSS Dictionary: " + dss);
            COSBase certsElement = dss.getDictionaryObject("Certs");
            if (certsElement instanceof COSArray) {
                printStreamsFromArray((COSArray) certsElement, "Cert");
            }
            COSBase ocspsElement = dss.getDictionaryObject("OCSPs");
            if (ocspsElement instanceof COSArray) {
                printStreamsFromArray((COSArray) ocspsElement, "Ocsp");
            }
            COSBase crlElement = dss.getDictionaryObject("CRLs");
            if (crlElement instanceof COSArray) {
                printStreamsFromArray((COSArray) crlElement, "CRL");
            }
        }
    }

    /**
     * Go through the elements of a COSArray containing each an COSStream to print in Hex.
     *
     * @param elements    COSArray of elements containing a COS Stream
     * @param description to append on Print
     * @throws IOException
     */
    private void printStreamsFromArray(COSArray elements, String description) throws IOException {
        for (COSBase baseElem : elements) {
            COSObject streamObj = (COSObject) baseElem;
            if (streamObj.getObject() instanceof COSStream) {
                COSStream cosStream = (COSStream) streamObj.getObject();
                InputStream input = cosStream.createInputStream();
                byte[] streamBytes = IOUtils.toByteArray(input);
                input.close();
                log.info(description + " (" + elements.indexOf(streamObj) + "): " + Hex.getString(streamBytes));
            }
        }
    }
}
