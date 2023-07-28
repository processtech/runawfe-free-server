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
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS12PfxPdu;
import org.bouncycastle.pkcs.PKCS12PfxPduBuilder;
import org.bouncycastle.pkcs.PKCS12SafeBag;
import org.bouncycastle.pkcs.PKCS12SafeBagBuilder;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.pkcs.PKCSException;
import org.bouncycastle.pkcs.bc.BcPKCS12MacCalculatorBuilder;
import org.bouncycastle.pkcs.bc.BcPKCS12PBEOutputEncryptorBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS12SafeBagBuilder;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class PKCS12Container extends RootPKCS12Container{
    private final DigitalSignature rootDigitalSignature;
    public PKCS12Container(DigitalSignature digitalSignature, DigitalSignature rootDigitalSignature) throws Exception {
        super(digitalSignature);
        this.rootDigitalSignature =  rootDigitalSignature;
    }
    @Override
    protected char [] getPassword () {
         return "nopassword".toCharArray();
    }
    @Override
    protected X500Name createIssuerDN() {
        X500NameBuilder nameBuilder = new X500NameBuilder();
        nameBuilder.addRDN(BCStyle.CN, rootDigitalSignature.getCommonName());
        nameBuilder.addRDN(BCStyle.C, rootDigitalSignature.getCountry());
        nameBuilder.addRDN(BCStyle.ST, rootDigitalSignature.getState());
        nameBuilder.addRDN(BCStyle.L, rootDigitalSignature.getCity());
        nameBuilder.addRDN(BCStyle.O, rootDigitalSignature.getOrganization());
        nameBuilder.addRDN(BCStyle.OU, rootDigitalSignature.getDepartment());
        nameBuilder.addRDN(BCStyle.EmailAddress, rootDigitalSignature.getEmail());
        return nameBuilder.build();
    }
    @Override
    protected void createCertificate() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, NoSuchProviderException, OperatorCreationException {
        RootPKCS12Container rootContainer = getRootContainer();
        KeyStore rootKeyStore = rootContainer.getKeyStore();
        String alias = rootKeyStore.aliases().nextElement();
        final Key rootPrivateKey = rootKeyStore.getKey(alias, super.getPassword());
        X509Certificate rootCertificate = rootContainer.getCertificate();

        final JcaX509v3CertificateBuilder certificateBuilder;
        certificateBuilder = new JcaX509v3CertificateBuilder(
            rootCertificate,
            BigInteger.valueOf(System.currentTimeMillis()),
            digitalSignature.getDateOfIssue(),
            digitalSignature.getDateOfExpiry(),
            createSubjectDN(),
            keyPair.getPublic());
            certificateBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
            certificateBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").setProvider(bcProvider).build(((PrivateKey)rootPrivateKey));
        final X509CertificateHolder holder = certificateBuilder.build(signer);
        x509Certificate = new JcaX509CertificateConverter().setProvider(bcProvider).getCertificate(holder);
    }
    @Override
    protected void fillContainer() throws CertificateException, IOException, NoSuchAlgorithmException, PKCSException, UnrecoverableKeyException, KeyStoreException, OperatorCreationException, NoSuchProviderException {
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        createCertificate();
        X509Certificate rootCertificate = getRootContainer().getCertificate();
        PublicKey rootPublicKey = rootCertificate.getPublicKey();

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        PKCS12SafeBagBuilder caCertBagBuilder = new JcaPKCS12SafeBagBuilder(rootCertificate);
        caCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("root_certificate"));
        caCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(rootPublicKey));


        PKCS12SafeBagBuilder eeCertBagBuilder = new JcaPKCS12SafeBagBuilder(x509Certificate);
        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_friendlyName, new DERBMPString("user_certificate"));
        eeCertBagBuilder.addBagAttribute(PKCSObjectIdentifiers.pkcs_9_at_localKeyId, extUtils.createSubjectKeyIdentifier(publicKey));

        PKCS12SafeBagBuilder keyBagBuilder = new JcaPKCS12SafeBagBuilder(privateKey,
                new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd3_KeyTripleDES_CBC,
                        new CBCBlockCipher(new DESedeEngine())).build(getPassword()));

        PKCS12SafeBag[] certs = new PKCS12SafeBag[2];

        certs[0] = eeCertBagBuilder.build();
        certs[1] = caCertBagBuilder.build();

        PKCS12PfxPduBuilder pfxPduBuilder = new PKCS12PfxPduBuilder();
        pfxPduBuilder.addEncryptedData(new BcPKCS12PBEOutputEncryptorBuilder(PKCSObjectIdentifiers.pbeWithSHAAnd40BitRC2_CBC, new CBCBlockCipher(new RC2Engine())).build(getPassword()), certs);
        pfxPduBuilder.addData(keyBagBuilder.build());

        PKCS12PfxPdu pkcs12PfxPdu = pfxPduBuilder.build(new BcPKCS12MacCalculatorBuilder(), getPassword());
        digitalSignature.setContainer(pkcs12PfxPdu.getEncoded());
    }
    private RootPKCS12Container getRootContainer() throws UnrecoverableKeyException, CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, NoSuchProviderException {
        RootPKCS12Container rootPKCS12Container = new RootPKCS12Container(rootDigitalSignature);
        rootPKCS12Container.updateUserDataFromContainer();
        return rootPKCS12Container;
    }
}
