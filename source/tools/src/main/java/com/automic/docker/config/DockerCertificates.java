/**
 * 
 */
package com.automic.docker.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import javax.net.ssl.HostnameVerifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;

import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;


/**
 * DockerCertificates holds certificates for connecting to an HTTPS-secured Docker instance with client/server
 * authentication.
 */
public class DockerCertificates {
    private static final Logger LOGGER = LogManager.getLogger(DockerCertificates.class);

    private static final String DEFAULT_CA_CERT_NAME = "ca.pem";
    private static final String DEFAULT_CLIENT_CERT_NAME = "cert.pem";
    private static final String DEFAULT_CLIENT_KEY_NAME = "key.pem";

    private static final char[] KEY_STORE_PASSWORD = "docker!!11!!one!".toCharArray();

    private File caCertPath;
    private File clientKeyPath;
    private File clientCertPath;

    private SSLContext sslContext;

    public DockerCertificates(final Path dockerCertPath) throws DockerException {

        File certDir = dockerCertPath.toFile();
        caCertPath = new File(certDir, DEFAULT_CA_CERT_NAME);
        clientKeyPath = new File(certDir, DEFAULT_CLIENT_KEY_NAME);
        clientCertPath = new File(certDir, DEFAULT_CLIENT_CERT_NAME);
        // Place check

        if (!caCertPath.exists() || !clientKeyPath.exists() || !clientCertPath.exists()) {
            throw new DockerException(String.format(ExceptionConstants.DOCKER_CERTIFICATE_MISSING,
                    DEFAULT_CA_CERT_NAME, DEFAULT_CLIENT_CERT_NAME, DEFAULT_CLIENT_KEY_NAME));
        }

        generateCertificates();
    }

    /**
     * Method to generate certificates for connection to Docker system. 
     * @throws DockerException
     */
    private void generateCertificates() throws DockerException {

        FileReader reader = null;
        try {
            final CertificateFactory cf = CertificateFactory.getInstance("X.509");
            final Certificate caCert = cf.generateCertificate(new FileInputStream(caCertPath));
            final Certificate clientCert = cf.generateCertificate(new FileInputStream(clientCertPath));

            reader = new FileReader(clientKeyPath);

            @SuppressWarnings("resource")
            final PEMKeyPair clientKeyPair = (PEMKeyPair) new PEMParser(reader).readObject();

            final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(clientKeyPair.getPrivateKeyInfo().getEncoded());
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            final PrivateKey clientKey = kf.generatePrivate(spec);

            final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            trustStore.setEntry("ca", new KeyStore.TrustedCertificateEntry(caCert), null);

            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("client", clientCert);
            keyStore.setKeyEntry("key", clientKey, KEY_STORE_PASSWORD, new Certificate[] { clientCert });

            this.sslContext = SSLContexts.custom().loadTrustMaterial(trustStore,null)
                    .loadKeyMaterial(keyStore, KEY_STORE_PASSWORD).useProtocol("TLS").build();

        } catch (CertificateException | IOException | NoSuchAlgorithmException | InvalidKeySpecException
                | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            LOGGER.error("Error in preparing https certificates ", e);
            throw new DockerException(ExceptionConstants.GENERIC_ERROR_MSG, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Error closing the reader ", e);
                }
            }
        }
    }

    /**
     * Method to get the instance of {@link SSLContext} for Docker connection
     * @return
     */
    public SSLContext sslContext() {

        return this.sslContext;
    }

    /**
     * Method to get the instance of {@link HostnameVerifier}  
     * @return
     */
    public HostnameVerifier hostnameVerifier() {
        return SSLConnectionSocketFactory.getDefaultHostnameVerifier();
    }

}


