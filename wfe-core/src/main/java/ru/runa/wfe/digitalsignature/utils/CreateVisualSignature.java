package ru.runa.wfe.digitalsignature.utils;

import com.google.common.base.Strings;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.apache.pdfbox.util.Matrix;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import ru.runa.wfe.commons.ClassLoaderUtil;

public class CreateVisualSignature extends CreateSignatureBase {
    protected final Log log = LogFactory.getLog(getClass());
    private SignatureOptions signatureOptions;
    private static final Properties signatureProperties = ClassLoaderUtil.getLocalizedProperties("signature", CreateVisualSignature.class, null);

    /**
     * Initialize the signature creator with a keystore (pkcs12) and pin that
     * should be used for the signature.
     *
     * @param keystore is a pkcs12 keystore.
     * @param pin      is the pin for the keystore / private key
     * @throws KeyStoreException         if the keystore has not been initialized (loaded)
     * @throws NoSuchAlgorithmException  if the algorithm for recovering the key cannot be found
     * @throws UnrecoverableKeyException if the given password is wrong
     * @throws CertificateException      if the certificate is not valid as signing time
     * @throws IOException               if no certificate could be found
     */
    public CreateVisualSignature(KeyStore keystore, char[] pin)
            throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, CertificateException {
        super(keystore, pin);
    }

    /**
     * @param file     The source pdf document file.
     * @param tsaUrl   optional TSA url
     * @param name     name of signer
     * @param reason   reason of signing
     * @param location place of signing
     * @return signedFile
     * @throws IOException
     */
    public byte[] signPDF(byte[] file, String signatureFieldName, String tsaUrl, String name, String location, String reason) throws IOException, CertificateEncodingException, NoSuchAlgorithmException {
        setTsaUrl(tsaUrl);
        PDDocument pdDocument = PDDocument.load(file);
        int numberOfSignatures = pdDocument.getSignatureDictionaries().size();
        int accessPermissions = SigUtils.getMDPPermission(pdDocument);
        if (accessPermissions == 1) {
            throw new IllegalStateException("No changes to the document are permitted due to DocMDP transform parameters dictionary");
        }
        PDSignature signature = null;
        PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm(null);
        PDRectangle rect = null;
        if (acroForm != null) {
            signature = findExistingSignature(acroForm, signatureFieldName);
            if (signature != null) {
                rect = acroForm.getField(signatureFieldName).getWidgets().get(0).getRectangle();
            }
        }
        if (signature == null) {
            // create signature dictionary
            signature = new PDSignature();
        }
        if (rect == null) {
            rect = createSignatureRectangle(pdDocument, numberOfSignatures);
        }
        if (acroForm != null && acroForm.getNeedAppearances()) {
            if (acroForm.getFields().isEmpty()) {
                acroForm.getCOSObject().removeItem(COSName.NEED_APPEARANCES);
            } else {
                log.info("/NeedAppearances is set, signature may be ignored by Adobe Reader");
            }
        }
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName(name);
        signature.setLocation(location);
        signature.setReason(reason);
        signature.setSignDate(Calendar.getInstance());

        SignatureInterface signatureInterface = this;
        signatureOptions = new SignatureOptions();
        signatureOptions.setVisualSignature(createVisualSignatureTemplate(pdDocument, 0, rect, signature));

        signatureOptions.setPage(0);
        pdDocument.addSignature(signature, signatureInterface, signatureOptions);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (Strings.isNullOrEmpty(tsaUrl)) {
            pdDocument.saveIncremental(output);
            pdDocument.close();
        } else {
            ExternalSigningSupport externalSigning = pdDocument.saveIncrementalForExternalSigning(output);
            byte[] cmsSignature = sign(externalSigning.getContent());
            externalSigning.setSignature(cmsSignature);
        }
        IOUtils.closeQuietly(signatureOptions);
        return output.toByteArray();
    }

    private PDRectangle createSignatureRectangle(PDDocument doc, int numberOfSignatures) {
        Rectangle2D humanRect = null;
        if (numberOfSignatures < 3) {
            humanRect = new Rectangle2D.Float(0 + numberOfSignatures * 190, 0, 190, 50);
        } else if (numberOfSignatures < 6) {
            humanRect = new Rectangle2D.Float(0 + (numberOfSignatures - 3) * 190, 50, 190, 50);
        } else {
            humanRect = new Rectangle2D.Float(0 + (numberOfSignatures - 6) * 190, 100, 190, 50);
        }
        float x = (float) humanRect.getX();
        float y = (float) humanRect.getY();
        float width = (float) humanRect.getWidth();
        float height = (float) humanRect.getHeight();
        PDPage page = doc.getPage(0);
        PDRectangle pageRect = page.getCropBox();
        PDRectangle rect = new PDRectangle();
        switch (page.getRotation()) {
            case 90:
                rect.setLowerLeftY(x);
                rect.setUpperRightY(x + width);
                rect.setLowerLeftX(y);
                rect.setUpperRightX(y + height);
                break;
            case 180:
                rect.setUpperRightX(pageRect.getWidth() - x);
                rect.setLowerLeftX(pageRect.getWidth() - x - width);
                rect.setLowerLeftY(y);
                rect.setUpperRightY(y + height);
                break;
            case 270:
                rect.setLowerLeftY(pageRect.getHeight() - x - width);
                rect.setUpperRightY(pageRect.getHeight() - x);
                rect.setLowerLeftX(pageRect.getWidth() - y - height);
                rect.setUpperRightX(pageRect.getWidth() - y);
                break;
            case 0:
            default:
                rect.setLowerLeftX(x);
                rect.setUpperRightX(x + width);
                rect.setLowerLeftY(pageRect.getHeight() - y - height);
                rect.setUpperRightY(pageRect.getHeight() - y);
                break;
        }
        return rect;
    }

    private InputStream createVisualSignatureTemplate(PDDocument srcDoc, int pageNum,
            PDRectangle rect, PDSignature signature) throws IOException, CertificateEncodingException, NoSuchAlgorithmException {
        PDDocument pdDocument = new PDDocument();
        PDPage page = new PDPage(srcDoc.getPage(pageNum).getMediaBox());
        pdDocument.addPage(page);
        PDAcroForm acroForm = new PDAcroForm(pdDocument);
        pdDocument.getDocumentCatalog().setAcroForm(acroForm);
        PDSignatureField signatureField = new PDSignatureField(acroForm);
        PDAnnotationWidget widget = signatureField.getWidgets().get(0);
        List<PDField> acroFormFields = acroForm.getFields();
        acroForm.setSignaturesExist(true);
        acroForm.setAppendOnly(true);
        acroForm.getCOSObject().setDirect(true);
        acroFormFields.add(signatureField);
        widget.setRectangle(rect);
        PDStream pdStream = new PDStream(pdDocument);
        PDFormXObject form = new PDFormXObject(pdStream);
        PDResources res = new PDResources();
        form.setResources(res);
        form.setFormType(1);
        PDRectangle bbox = new PDRectangle(rect.getWidth(), rect.getHeight());
        float height = bbox.getHeight();
        Matrix initialScale = null;
        switch (srcDoc.getPage(pageNum).getRotation()) {
            case 90:
                form.setMatrix(AffineTransform.getQuadrantRotateInstance(1));
                initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                height = bbox.getWidth();
                break;
            case 180:
                form.setMatrix(AffineTransform.getQuadrantRotateInstance(2));
                break;
            case 270:
                form.setMatrix(AffineTransform.getQuadrantRotateInstance(3));
                initialScale = Matrix.getScaleInstance(bbox.getWidth() / bbox.getHeight(), bbox.getHeight() / bbox.getWidth());
                height = bbox.getWidth();
                break;
            case 0:
            default:
                break;
        }

        form.setBBox(bbox);
        PDFont font = PDType0Font.load(pdDocument, PDDocument.class.getResourceAsStream(
                "/org/apache/pdfbox/resources/ttf/LiberationSans-Regular.ttf"), false);
        PDAppearanceDictionary appearance = new PDAppearanceDictionary();
        appearance.getCOSObject().setDirect(true);
        PDAppearanceStream appearanceStream = new PDAppearanceStream(form.getCOSObject());
        appearance.setNormalAppearance(appearanceStream);
        widget.setAppearance(appearance);
        PDPageContentStream cs = new PDPageContentStream(pdDocument, appearanceStream);
        if (initialScale != null) {
            cs.transform(initialScale);
        }
        PDImageXObject pdImageXObject = PDImageXObject.createFromByteArray(pdDocument, VisualStamp.getImage(), "stamp.png");
        cs.drawImage(pdImageXObject, 0, 0);
        float fontSize = 8;
        float leading = 7;
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(Color.BLUE);
        cs.beginText();
        cs.newLineAtOffset(4, height - 9);
        cs.setLeading(leading);
        X509Certificate cert = (X509Certificate) getCertificateChain()[0];
        X500Name x500Name = new X500Name(cert.getSubjectX500Principal().getName());
        RDN cn = x500Name.getRDNs(BCStyle.CN)[0];
        String name = IETFUtils.valueToString(cn.getFirst().getValue());
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        String md5Fingerprint = javax.xml.bind.DatatypeConverter.printHexBinary(messageDigest.digest(cert.getEncoded()));
        cs.showText(signatureProperties.getProperty("document.signed"));
        cs.newLine();
        cs.showText(signatureProperties.getProperty("digital.signature"));
        cs.newLine();
        cs.setFont(font, 7);
        cs.showText(signatureProperties.getProperty("certificate") + " " + md5Fingerprint);
        cs.newLine();
        cs.showText(signatureProperties.getProperty("owner") + " " + name);
        cs.newLine();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        cs.showText(signatureProperties.getProperty("valid.from") + " "
                + simpleDateFormat.format(cert.getNotBefore().getTime()) + " "
                + signatureProperties.getProperty("valid.to") + " "
                + simpleDateFormat.format(cert.getNotAfter().getTime()));
        cs.endText();
        cs.close();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdDocument.save(baos);
        pdDocument.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private PDSignature findExistingSignature(PDAcroForm acroForm, String sigFieldName) {
        PDSignature signature = null;
        PDSignatureField signatureField;
        if (acroForm != null) {
            signatureField = (PDSignatureField) acroForm.getField(sigFieldName);
            if (signatureField != null) {
                signature = signatureField.getSignature();
                if (signature == null) {
                    signature = new PDSignature();
                    signatureField.getCOSObject().setItem(COSName.V, signature);
                } else {
                    throw new IllegalStateException("The signature field " + sigFieldName + " is already signed.");
                }
            }
        }
        return signature;
    }
}
