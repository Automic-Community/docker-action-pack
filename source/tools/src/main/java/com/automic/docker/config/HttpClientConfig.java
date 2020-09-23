package com.automic.docker.config;

import java.io.File;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * Configuration class for http client.This class sets the parameters like connection timeout , read time out , handling
 * of cookies and whether to retry or not
 * */
public final class HttpClientConfig {

    private static final Logger LOGGER = LogManager.getLogger(HttpClientConfig.class);

    private HttpClientConfig() {
    }

    /**
     * Creates an instance of {@link Client} using parameters specified.  
     * @param protocol either http or https
     * @param certificatePath path to connection certificates
     * @param connectionTimeOut timeout in milliseconds
     * @param readTimeOut read timeout in milliseconds
     * @return an instance of {@link Client}
     * @throws DockerException
     */
    public static Client getClient(String protocol, String certificatePath, int connectionTimeOut, int readTimeOut)
            throws DockerException {
        Client client;

        ClientConfig config = new DefaultClientConfig();

       
        config.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, connectionTimeOut);
        config.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, readTimeOut);

        if (Constants.HTTPS.equalsIgnoreCase(protocol)) {
            validateCertificates(certificatePath, config);
        }
        client = Client.create(config);

        return client;
    }

    /**
     * Method to validate certificates specified at system path with that of the Docker URL specified.
     * @param certificatePath Path to certificates
     * @param config config to Docker connection
     * @throws DockerException
     */
    private static void validateCertificates(String certificatePath, ClientConfig config)
            throws DockerException {

        if (!certificatePath.isEmpty()) {
            File tmpFile = new File(certificatePath);

            if (tmpFile.getParentFile() == null || !tmpFile.getParentFile().exists()) {
                LOGGER.error("Invalid docker-certificate-path : " + certificatePath);
                throw new DockerException(ExceptionConstants.DOCKER_CERTIFICATE_MISSING);
            }
        } else {
            LOGGER.error(ExceptionConstants.EMPTY_DOCKER_CERITIFCATE_PATH);
            throw new DockerException(ExceptionConstants.EMPTY_DOCKER_CERITIFCATE_PATH);
        }
        DockerCertificates certs = new DockerCertificates(Paths.get(certificatePath));
        HTTPSProperties props = new HTTPSProperties(certs.hostnameVerifier(), certs.sslContext());
        config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, props);

    }

}
