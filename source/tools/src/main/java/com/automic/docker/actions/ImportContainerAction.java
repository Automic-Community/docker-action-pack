/**
 *
 */
package com.automic.docker.actions;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.automic.docker.constants.Constants;
import com.automic.docker.constants.ExceptionConstants;
import com.automic.docker.exceptions.DockerException;
import com.automic.docker.utility.DockerUtility;
import com.automic.docker.utility.Validator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * This class imports the TAR file containing the contents of exported container as an image in the given docker system.
 * User needs to provide the exported TAR file and the name of the image that needs to be created.The contents of the
 * TAR file are not validated.This action creates an image only user needs to create the container by providing the
 * required configuration settings.In order to create and run the container user can use create and start actions
 */
public class ImportContainerAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(ImportContainerAction.class);

    private static final int NO_OF_ARGS = 6;

    private static final int TAR_FILE_INDEX = 4;
    private static final int IMAGE_INDEX = 5;

    private static final String APPLICATION_TAR = "application/tar";
    private String tarFilePath;
    private String imageName;

    public ImportContainerAction() {
        super(NO_OF_ARGS);
    }

    @Override
    protected void logParameters(String[] args) {
        LOGGER.info("Input parameters -->");
        LOGGER.info("Connection Timeout = "
                + ((args.length > CONNECTION_TIMEOUT_INDEX) ? args[CONNECTION_TIMEOUT_INDEX] : ""));
        LOGGER.info("Read-timeout = " + ((args.length > READ_TIMEOUT_INDEX) ? args[READ_TIMEOUT_INDEX] : ""));
        LOGGER.info("Docker-url = " + ((args.length > DOCKER_URL_INDEX) ? args[DOCKER_URL_INDEX] : ""));
        LOGGER.info("Certificate-path = " + ((args.length > CERTIFICATE_INDEX) ? args[CERTIFICATE_INDEX] : ""));
        LOGGER.info("Tar File Path = " + ((args.length > TAR_FILE_INDEX) ? args[TAR_FILE_INDEX] : ""));
        LOGGER.info("Image Name = " + ((args.length > IMAGE_INDEX) ? args[IMAGE_INDEX] : ""));

    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        tarFilePath = args[TAR_FILE_INDEX];
        imageName = args[IMAGE_INDEX];

    }

    /**
     * This function checks if the given TAR file exists or not and the given image name must not be empty string. It
     * will throw DockerException if any of the above case fails
     */
    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkFileExistsAndIsFile(tarFilePath)) {
            String msg = String.format(ExceptionConstants.INVALID_FILE, tarFilePath);
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

        if (imageName.isEmpty()) {
            LOGGER.error(ExceptionConstants.EMPTY_REPO_NAME);
            throw new DockerException(ExceptionConstants.EMPTY_REPO_NAME);
        }

    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("images").path("create").queryParam("fromSrc", "-");

        int lastColon = imageName.lastIndexOf(':');
        if (lastColon > -1) {
            String tag = imageName.substring(lastColon + 1);
            if (tag.indexOf('/') < 0) {
                imageName = imageName.substring(0, lastColon);
                webResource = webResource.queryParam("repo", imageName).queryParam("tag", tag);
            } else {
                webResource = webResource.queryParam("repo", imageName);
            }

        } else {
            webResource = webResource.queryParam("repo", imageName);
        }

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.entity(new File(tarFilePath), APPLICATION_TAR).accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        return (HttpStatus.SC_INTERNAL_SERVER_ERROR == errorCode) ? "server error " : Constants.UNKNOWN_ERROR;
    }

    /**
     * This function will print the value of json containing the key as status or error
     */
    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        DockerUtility.readJsonResponseFromStream(response);
    }
}
