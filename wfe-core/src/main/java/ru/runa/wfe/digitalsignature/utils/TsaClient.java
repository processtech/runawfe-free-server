package ru.runa.wfe.digitalsignature.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

public class TsaClient {
    private static final Log log = LogFactory.getLog(TsaClient.class);

    private final URL url;
    private final String username;
    private final String password;
    private final MessageDigest digest;
    private static final Random RANDOM = new SecureRandom();

    /**
     * @param url      the URL of the TSA service
     * @param username user name of TSA
     * @param password password of TSA
     * @param digest   the message digest to use
     */
    public TsaClient(URL url, String username, String password, MessageDigest digest) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.digest = digest;
    }

    /**
     * @param content
     * @return the time stamp token
     * @throws IOException if there was an error with the connection or data from the TSA server,
     *                     or if the time stamp response could not be validated
     */
    public TimeStampToken getTimeStampToken(byte[] content) throws IOException {
        digest.reset();
        byte[] hash = digest.digest(content);
        int nonce = RANDOM.nextInt();
        TimeStampRequestGenerator tsaGenerator = new TimeStampRequestGenerator();
        tsaGenerator.setCertReq(true);
        ASN1ObjectIdentifier oid = getHashObjectIdentifier(digest.getAlgorithm());
        TimeStampRequest request = tsaGenerator.generate(oid, hash, BigInteger.valueOf(nonce));
        byte[] tsaResponse = getTsaResponse(request.getEncoded());
        TimeStampResponse response;
        try {
            response = new TimeStampResponse(tsaResponse);
            response.validate(request);
        } catch (TSPException e) {
            throw new IOException(e);
        }
        TimeStampToken timeStampToken = response.getTimeStampToken();
        if (timeStampToken == null) {
            throw new IOException("Response from " + url +
                    " does not have a time stamp token, status: " + response.getStatus() +
                    " (" + response.getStatusString() + ")");
        }
        return timeStampToken;
    }

    private byte[] getTsaResponse(byte[] request) throws IOException {
        log.debug("Opening connection to TSA server");
        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/timestamp-query");
        log.debug("Established connection to TSA server");
        if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
            throw new UnsupportedOperationException("authentication not implemented yet");
        }
        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(request);
        } catch (IOException ex) {
            log.error("Exception when writing to " + this.url, ex);
            throw ex;
        } finally {
            IOUtils.closeQuietly(output);
        }

        log.debug("Waiting for response from TSA server");

        InputStream input = null;
        byte[] response;
        try {
            input = connection.getInputStream();
            response = IOUtils.toByteArray(input);
        } catch (IOException ex) {
            log.error("Exception when reading from " + this.url, ex);
            throw ex;
        } finally {
            IOUtils.closeQuietly(input);
        }

        log.debug("Received response from TSA server");

        return response;
    }

    private ASN1ObjectIdentifier getHashObjectIdentifier(String algorithm) {
        if (algorithm.equals("MD2")) {
            return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md2.getId());
        } else if (algorithm.equals("MD5")) {
            return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md5.getId());
        } else if (algorithm.equals("SHA-1")) {
            return new ASN1ObjectIdentifier(OIWObjectIdentifiers.idSHA1.getId());
        } else if (algorithm.equals("SHA-224")) {
            return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha224.getId());
        } else if (algorithm.equals("SHA-256")) {
            return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha256.getId());
        } else if (algorithm.equals("SHA-384")) {
            return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha384.getId());
        } else if (algorithm.equals("SHA-512")) {
            return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha512.getId());
        } else {
            return new ASN1ObjectIdentifier(algorithm);
        }
    }
}
