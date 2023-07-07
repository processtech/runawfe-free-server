package ru.runa.wfe.digitalsignature.utils;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.bouncycastle.asn1.DERBMPString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder;
import org.bouncycastle.pkcs.PKCS12SafeBagBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.digitalsignature.DigitalSignature;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RootPKCS12Container {
    protected KeyPair keyPair;
    protected X509Certificate x509Certificate;
    private KeyStore keyStore;
    protected DigitalSignature digitalSignature;
    protected final Provider bcProvider = new BouncyCastleProvider();

    public RootPKCS12Container(DigitalSignature digitalSignature) throws NullPointerException, SecurityException {
        this.digitalSignature = digitalSignature;
        Security.addProvider(bcProvider);
    }
    protected char[] getPassword(){
        return SystemProperties.getSignatureServerContainerPassword().toCharArray();
    }

    KeyPair createKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        return keyGen.generateKeyPair();
    }

    protected X500Name createSubjectDN() {
        X500NameBuilder nameBuilder = new X500NameBuilder();
        nameBuilder.addRDN(BCStyle.CN, digitalSignature.getCommonName());
        nameBuilder.addRDN(BCStyle.C, digitalSignature.getCountry());
        nameBuilder.addRDN(BCStyle.ST, digitalSignature.getState());
        nameBuilder.addRDN(BCStyle.L, digitalSignature.getCity());
        nameBuilder.addRDN(BCStyle.O, digitalSignature.getOrganization());
        nameBuilder.addRDN(BCStyle.OU, digitalSignature.getDepartment());
        nameBuilder.addRDN(BCStyle.EmailAddress, digitalSignature.getEmail());
        return nameBuilder.build();
    }

    protected X500Name createIssuerDN() {
        return createSubjectDN();
    } //root certificate is issued by himself

    protected void createCertificate() throws IOException, CertificateException, OperatorCreationException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
        final X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(
                createIssuerDN(),
                BigInteger.valueOf(System.currentTimeMillis()),
                digitalSignature.getDateOfIssue(),
                digitalSignature.getDateOfExpiry(),
                createSubjectDN(),
                SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));
                certificateBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.keyCertSign));
                certificateBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints(true));
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(bcProvider).build(keyPair.getPrivate());
        final X509CertificateHolder holder = certificateBuilder.build(signer);
        x509Certificate = new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(holder);
    }

    public void createContainer()  throws Exception {
        digitalSignature.setContainer(null);
        keyPair = createKeyPair();
        fillContainer();
    }

    protected void fillContainer() throws CertificateException, IOException, NoSuchAlgorithmException, PKCSException, UnrecoverableKeyException, KeyStoreException, OperatorCreationException, NoSuchProviderException {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        createCertificate();

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        PKCS12SafeBagBuilder eeCertBagBuilder = new JcaPKCS12SafeBagBuilder(x509Certificate);
        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("certificate"));
        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(privateKey,
                new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC,
                        new CBCBlockCipher(new DESedeEngine())).build(getPassword()));

        PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();
        pfxPduBuilder.addEncryptedData(new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, new CBCBlockCipher(new RC2Engine())).build(getPassword()), eeCertBagBuilder.build());
        pfxPduBuilder.addData(keyBagBuilder.build());

        PKCS12PfxPdu pkcs12PfxPdu = pfxPduBuilder.build(new BcPKCS12MacCalculatorBuilder(), getPassword());
        digitalSignature.setContainer(pkcs12PfxPdu.getEncoded());
    }

    public void updateUserDataFromContainer() throws KeyStoreException, NoSuchProviderException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {
        keyStore = KeyStore.getInstance("pkcs12", BouncyCastleProvider.PROVIDER_NAME);
        keyStore.load(new ByteArrayInputStream(digitalSignature.getContainer()), getPassword());
        Enumeration<String> aliases = keyStore.aliases();
        String alias = aliases.nextElement();
        x509Certificate = (X509Certificate) keyStore.getCertificate(alias);
        Key privateKey = keyStore.getKey(alias, getPassword());
        if (privateKey instanceof PrivateKey) {
            PublicKey pubKey = x509Certificate.getPublicKey();
            keyPair = new KeyPair(pubKey, (PrivateKey) privateKey);
        }
        fillDsFields(x509Certificate);
    }

    private void fillDsFields(X509Certificate certificate) {
        X500Principal dSDetails = certificate.getSubjectX500Principal();
        Map<String, String> dsFields = new HashMap<>();
        for (String s : dSDetails.toString().split(",")) {
            String[] kvPair = s.trim().split("=");
            if (kvPair.length > 1) {
                dsFields.put(kvPair[0], kvPair[1]);
            } else {
                dsFields.put(kvPair[0], "");
            }
        }
        digitalSignature.setCommonName(dsFields.get("CN"));
        digitalSignature.setEmail(dsFields.get("EMAILADDRESS"));
        digitalSignature.setDepartment(dsFields.get("OU"));
        digitalSignature.setOrganization(dsFields.get("O"));
        digitalSignature.setCity(dsFields.get("L"));
        digitalSignature.setState(dsFields.get("ST"));
        digitalSignature.setCountry(dsFields.get("C"));
        digitalSignature.setDateOfIssue(certificate.getNotBefore());
        digitalSignature.setDateOfExpiry(certificate.getNotAfter());
        digitalSignature.countValidity();
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public X509Certificate getCertificate() {
        return x509Certificate;
    }

    public void updateContainer() throws KeyStoreException, NoSuchProviderException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, PKCSException, OperatorCreationException {
        keyStore = KeyStore.getInstance("pkcs12", BouncyCastleProvider.PROVIDER_NAME);
        keyStore.load(new ByteArrayInputStream(digitalSignature.getContainer()), getPassword());
        Enumeration<String> aliases = keyStore.aliases();
        String alias = aliases.nextElement();
        x509Certificate = (X509Certificate) keyStore.getCertificate(alias);
        Key privateKey = keyStore.getKey(alias, getPassword());
        if (privateKey instanceof PrivateKey) {
            PublicKey pubKey = x509Certificate.getPublicKey();
            keyPair = new KeyPair(pubKey, (PrivateKey) privateKey);
        }
        fillContainer();
    }
}
