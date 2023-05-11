package ru.runa.wfe.digitalsignature.utils.cert;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.security.cert.X509Extension;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ocsp.OCSPObjectIdentifiers;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.ocsp.BasicOCSPResp;
import org.bouncycastle.cert.ocsp.OCSPException;
import org.bouncycastle.cert.ocsp.OCSPResp;
import ru.runa.wfe.digitalsignature.utils.SigUtils;

public final class CertificateVerifier {
    private static final Log log = LogFactory.getLog(CertificateVerifier.class);

    private CertificateVerifier() {
    }

    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates and intermediate
     * certificates that will be used for building the certification chain. The
     * verification process assumes that all self-signed certificates in the set
     * are trusted root CA certificates and all other certificates in the set
     * are intermediate certificates.
     *
     * @param cert                 - certificate for validation
     * @param additionalCerts      - set of trusted root CA certificates that will be
     *                             used as "trust anchors" and intermediate CA certificates that will be
     *                             used as part of the certification chain. All self-signed certificates are
     *                             considered to be trusted root CA certificates. All the rest are
     *                             considered to be intermediate CA certificates.
     * @param verifySelfSignedCert true if a self-signed certificate is accepted, false if not.
     * @param signDate             the date when the signing took place
     * @return the certification chain (if verification is successful)
     * @throws CertificateVerificationException - if the certification is not
     *                                          successful (e.g. certification path cannot be built or some certificate
     *                                          in the chain is expired or CRL checks are failed)
     */
    public static PKIXCertPathBuilderResult verifyCertificate(
            X509Certificate cert, Set<X509Certificate> additionalCerts,
            boolean verifySelfSignedCert, Date signDate)
            throws CertificateVerificationException {
        try {
            if (!verifySelfSignedCert && isSelfSigned(cert)) {
                throw new CertificateVerificationException("The certificate is self-signed.");
            }

            Set<X509Certificate> certSet = new HashSet<X509Certificate>(additionalCerts);
            Set<X509Certificate> certsToTrySet = new HashSet<X509Certificate>();
            certsToTrySet.add(cert);
            certsToTrySet.addAll(additionalCerts);
            int downloadSize = 0;
            while (!certsToTrySet.isEmpty()) {
                Set<X509Certificate> nextCertsToTrySet = new HashSet<X509Certificate>();
                for (X509Certificate tryCert : certsToTrySet) {
                    Set<X509Certificate> downloadedExtraCertificatesSet =
                            CertificateVerifier.downloadExtraCertificates(tryCert);
                    for (X509Certificate downloadedCertificate : downloadedExtraCertificatesSet) {
                        if (!certSet.contains(downloadedCertificate)) {
                            nextCertsToTrySet.add(downloadedCertificate);
                            certSet.add(downloadedCertificate);
                            downloadSize++;
                        }
                    }
                }
                certsToTrySet = nextCertsToTrySet;
            }
            if (downloadSize > 0) {
                log.info("CA issuers: " + downloadSize + " downloaded certificate(s) are new");
            }
            Set<X509Certificate> intermediateCerts = new HashSet<X509Certificate>();
            Set<TrustAnchor> trustAnchors = new HashSet<TrustAnchor>();
            for (X509Certificate additionalCert : certSet) {
                if (isSelfSigned(additionalCert)) {
                    trustAnchors.add(new TrustAnchor(additionalCert, null));
                } else {
                    intermediateCerts.add(additionalCert);
                }
            }
            if (trustAnchors.isEmpty()) {
                throw new CertificateVerificationException("No root certificate in the chain");
            }
            PKIXCertPathBuilderResult verifiedCertChain = verifyCertificate(
                    cert, trustAnchors, intermediateCerts, signDate);
            log.info("Certification chain verified successfully up to this root: " +
                    verifiedCertChain.getTrustAnchor().getTrustedCert().getSubjectX500Principal());
            checkRevocations(cert, certSet, signDate);
            return verifiedCertChain;
        } catch (CertPathBuilderException certPathEx) {
            throw new CertificateVerificationException(
                    "Error building certification path: "
                            + cert.getSubjectX500Principal(), certPathEx);
        } catch (CertificateVerificationException cvex) {
            throw cvex;
        } catch (Exception ex) {
            throw new CertificateVerificationException(
                    "Error verifying the certificate: "
                            + cert.getSubjectX500Principal(), ex);
        }
    }

    private static void checkRevocations(X509Certificate cert,
            Set<X509Certificate> additionalCerts,
            Date signDate)
            throws IOException, CertificateVerificationException, OCSPException,
            RevokedCertificateException, GeneralSecurityException {
        if (isSelfSigned(cert)) {
            return;
        }
        for (X509Certificate additionalCert : additionalCerts) {
            try {
                cert.verify(additionalCert.getPublicKey(), SecurityProvider.getProvider().getName());
                checkRevocationsWithIssuer(cert, additionalCert, additionalCerts, signDate);
            } catch (GeneralSecurityException ex) {
                // not the issuer
            }
        }
    }

    private static void checkRevocationsWithIssuer(X509Certificate cert, X509Certificate issuerCert,
            Set<X509Certificate> additionalCerts, Date signDate)
            throws CertificateVerificationException, IOException, RevokedCertificateException,
            GeneralSecurityException, OCSPException {
        String ocspURL = extractOCSPURL(cert);
        if (ocspURL != null) {
            OcspHelper ocspHelper = new OcspHelper(cert, signDate, issuerCert, additionalCerts, ocspURL);
            try {
                verifyOCSP(ocspHelper, additionalCerts);
            } catch (IOException ex) {
                log.warn("IOException trying OCSP, will try CRL", ex);
                log.warn("Certificate# to check: " + cert.getSerialNumber().toString(16));
                CRLVerifier.verifyCertificateCRLs(cert, signDate, additionalCerts);
            } catch (OCSPException ex) {
                log.warn("OCSPException trying OCSP, will try CRL", ex);
                log.warn("Certificate# to check: " + cert.getSerialNumber().toString(16));
                CRLVerifier.verifyCertificateCRLs(cert, signDate, additionalCerts);
            }
        } else {
            log.info("OCSP not available, will try CRL");
            CRLVerifier.verifyCertificateCRLs(cert, signDate, additionalCerts);
        }
        checkRevocations(issuerCert, additionalCerts, signDate);
    }

    /**
     * Checks whether given X.509 certificate is self-signed.
     *
     * @param cert The X.509 certificate to check.
     * @return true if the certificate is self-signed, false if not.
     * @throws java.security.GeneralSecurityException
     */
    public static boolean isSelfSigned(X509Certificate cert) throws GeneralSecurityException {
        try {
            PublicKey key = cert.getPublicKey();
            cert.verify(key, SecurityProvider.getProvider().getName());
            return true;
        } catch (SignatureException ex) {
            log.debug("Couldn't get signature information - returning false", ex);
            return false;
        } catch (InvalidKeyException ex) {
            log.debug("Couldn't get signature information - returning false", ex);
            return false;
        } catch (IOException ex) {
            log.debug("Couldn't get signature information - returning false", ex);
            return false;
        }
    }

    /**
     * Download extra certificates from the URI mentioned in id-ad-caIssuers in the "authority
     * information access" extension. The method is lenient, i.e. catches all exceptions.
     *
     * @param ext an X509 object that can have extensions.
     * @return a certificate set, never null.
     */
    public static Set<X509Certificate> downloadExtraCertificates(X509Extension ext) {
        Set<X509Certificate> resultSet = new HashSet<X509Certificate>();
        byte[] authorityExtensionValue = ext.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (authorityExtensionValue == null) {
            return resultSet;
        }
        ASN1Primitive asn1Prim;
        try {
            asn1Prim = JcaX509ExtensionUtils.parseExtensionValue(authorityExtensionValue);
        } catch (IOException ex) {
            log.warn(ex.getMessage(), ex);
            return resultSet;
        }
        if (!(asn1Prim instanceof ASN1Sequence)) {
            log.warn("ASN1Sequence expected, got " + asn1Prim.getClass().getSimpleName());
            return resultSet;
        }
        ASN1Sequence asn1Seq = (ASN1Sequence) asn1Prim;
        Enumeration<?> objects = asn1Seq.getObjects();
        while (objects.hasMoreElements()) {
            // AccessDescription
            ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
            ASN1Encodable oid = obj.getObjectAt(0);
            if (!X509ObjectIdentifiers.id_ad_caIssuers.equals(oid)) {
                continue;
            }
            ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);
            ASN1OctetString uri = (ASN1OctetString) location.getObject();
            String urlString = new String(uri.getOctets());
            InputStream in = null;
            try {
                log.info("CA issuers URL: " + urlString);
                in = SigUtils.openURL(urlString);
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                Collection<? extends Certificate> altCerts = certFactory.generateCertificates(in);
                for (Certificate altCert : altCerts) {
                    resultSet.add((X509Certificate) altCert);
                }
                log.info("CA issuers URL: " + altCerts.size() + " certificate(s) downloaded");
            } catch (IOException ex) {
                log.warn(urlString + " failure: " + ex.getMessage(), ex);
            } catch (CertificateException ex) {
                log.warn(ex.getMessage(), ex);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        log.info("CA issuers: Downloaded " + resultSet.size() + " certificate(s) total");
        return resultSet;
    }

    /**
     * Attempts to build a certification chain for given certificate and to
     * verify it. Relies on a set of root CA certificates (trust anchors) and a
     * set of intermediate certificates (to be used as part of the chain).
     *
     * @param cert              - certificate for validation
     * @param trustAnchors      - set of trust anchors
     * @param intermediateCerts - set of intermediate certificates
     * @param signDate          the date when the signing took place
     * @return the certification chain (if verification is successful)
     * @throws GeneralSecurityException - if the verification is not successful
     *                                  (e.g. certification path cannot be built or some certificate in the chain
     *                                  is expired)
     */
    private static PKIXCertPathBuilderResult verifyCertificate(
            X509Certificate cert, Set<TrustAnchor> trustAnchors,
            Set<X509Certificate> intermediateCerts, Date signDate)
            throws GeneralSecurityException {
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);
        PKIXBuilderParameters pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
        pkixParams.setRevocationEnabled(false);
        pkixParams.setPolicyQualifiersRejected(false);
        pkixParams.setDate(signDate);
        CertStore intermediateCertStore = CertStore.getInstance("Collection",
                new CollectionCertStoreParameters(intermediateCerts));
        pkixParams.addCertStore(intermediateCertStore);
        CertPathBuilder builder = CertPathBuilder.getInstance("PKIX");
        return (PKIXCertPathBuilderResult) builder.build(pkixParams);
    }

    /**
     * Extract the OCSP URL from an X.509 certificate if available.
     *
     * @param cert X.509 certificate
     * @return the URL of the OCSP validation service
     * @throws IOException
     */
    private static String extractOCSPURL(X509Certificate cert) throws IOException {
        byte[] authorityExtensionValue = cert.getExtensionValue(Extension.authorityInfoAccess.getId());
        if (authorityExtensionValue != null) {
            ASN1Sequence asn1Seq = (ASN1Sequence) JcaX509ExtensionUtils.parseExtensionValue(authorityExtensionValue);
            Enumeration<?> objects = asn1Seq.getObjects();
            while (objects.hasMoreElements()) {
                ASN1Sequence obj = (ASN1Sequence) objects.nextElement();
                ASN1Encodable oid = obj.getObjectAt(0);
                ASN1TaggedObject location = (ASN1TaggedObject) obj.getObjectAt(1);
                if (X509ObjectIdentifiers.id_ad_ocsp.equals(oid)
                        && location.getTagNo() == GeneralName.uniformResourceIdentifier) {
                    ASN1OctetString url = (ASN1OctetString) location.getObject();
                    String ocspURL = new String(url.getOctets());
                    log.info("OCSP URL: " + ocspURL);
                    return ocspURL;
                }
            }
        }
        return null;
    }

    /**
     * Verify whether the certificate has been revoked at signing date, and verify whether the
     * certificate of the responder has been revoked now.
     *
     * @param ocspHelper      the OCSP helper.
     * @param additionalCerts
     * @throws RevokedCertificateException
     * @throws IOException
     * @throws OCSPException
     * @throws CertificateVerificationException
     */
    private static void verifyOCSP(OcspHelper ocspHelper, Set<X509Certificate> additionalCerts)
            throws RevokedCertificateException, IOException, OCSPException, CertificateVerificationException {
        Date now = Calendar.getInstance().getTime();
        OCSPResp ocspResponse;
        ocspResponse = ocspHelper.getResponseOcsp();
        if (ocspResponse.getStatus() != OCSPResp.SUCCESSFUL) {
            throw new CertificateVerificationException("OCSP check not successful, status: "
                    + ocspResponse.getStatus());
        }
        log.info("OCSP check successful");
        BasicOCSPResp basicResponse = (BasicOCSPResp) ocspResponse.getResponseObject();
        X509Certificate ocspResponderCertificate = ocspHelper.getOcspResponderCertificate();
        if (ocspResponderCertificate.getExtensionValue(OCSPObjectIdentifiers.id_pkix_ocsp_nocheck.getId()) != null) {
            log.info("Revocation check of OCSP responder certificate skipped (id-pkix-ocsp-nocheck is set)");
            return;
        }
        if (ocspHelper.getCertificateToCheck().equals(ocspResponderCertificate)) {
            log.info("OCSP responder certificate is identical to certificate to check");
            return;
        }
        log.info("Check of OCSP responder certificate");
        Set<X509Certificate> additionalCerts2 = new HashSet<X509Certificate>(additionalCerts);
        JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();
        for (X509CertificateHolder certHolder : basicResponse.getCerts()) {
            try {
                X509Certificate cert = certificateConverter.getCertificate(certHolder);
                if (!ocspResponderCertificate.equals(cert)) {
                    additionalCerts2.add(cert);
                }
            } catch (CertificateException ex) {
                log.error(ex, ex);
            }
        }
        CertificateVerifier.verifyCertificate(ocspResponderCertificate, additionalCerts2, true, now);
        log.info("Check of OCSP responder certificate done");
    }
}
