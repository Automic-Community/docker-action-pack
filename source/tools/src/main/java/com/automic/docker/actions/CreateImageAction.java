/**
 *
 */
package com.automic.docker.actions;

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
 * Action class to create an image by pulling it from docker public repository. By default Image tag value is 'latest'.
 * It will pull latest image as per the specified image name with image tag value 'latest'. It will pull all the images
 * for the specified image name if tag is unspecified.
 *
 */
public class CreateImageAction extends AbstractAction {

    private static final Logger LOGGER = LogManager.getLogger(CreateImageAction.class);

    private static final int NO_OF_ARGS = 5;

    private static final int IMAGE_NAME_INDEX = 4;
    private static final int IMAGE_TAG_INDEX = 5;

    private String imageName;
    private String imageTag;

    public CreateImageAction() {
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
        LOGGER.info("Image Name = " + ((args.length > IMAGE_NAME_INDEX) ? args[IMAGE_NAME_INDEX] : ""));
        LOGGER.info("Tag = " + ((args.length > IMAGE_TAG_INDEX) ? args[IMAGE_TAG_INDEX] : ""));
    }

    @Override
    protected void initialize(String[] args) throws DockerException {
        imageName = args[IMAGE_NAME_INDEX];
        if (args.length > IMAGE_TAG_INDEX) {
            imageTag = args[IMAGE_TAG_INDEX];
        }
    }

    @Override
    protected void validateInputs() throws DockerException {
        if (!Validator.checkNotEmpty(imageName)) {
            String msg = String.format(ExceptionConstants.MISSING_REQUIRED_PARAM, "Image name");
            LOGGER.error(msg);
            throw new DockerException(msg);
        }

    }

    @Override
    protected ClientResponse executeSpecific(Client client) throws DockerException {

        ClientResponse response = null;

        WebResource webResource = client.resource(dockerUrl).path("images").path("create")
                .queryParam("fromImage", imageName);
        if (Validator.checkNotEmpty(imageTag)) {
            webResource = webResource.queryParam("tag", imageTag);
        }

        LOGGER.info("Calling url " + webResource.getURI());

        response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class);

        return response;
    }

    @Override
    protected String getErrorMessage(int errorCode) {
        return (HttpStatus.SC_INTERNAL_SERVER_ERROR == errorCode) ? "server error " : Constants.UNKNOWN_ERROR;
    }

    @Override
    protected void prepareOutput(ClientResponse response) throws DockerException {
        DockerUtility.readJsonResponseFromStream(response);
    }

}
